package com.als.webIde.service;

import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.MemberRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j // 롬복 Logger
@RequiredArgsConstructor // final 처리
@Service
public class DockerServiceImpl implements DockerService {
    private final DockerClient dockerClient;
    private final MemberRepository memberRepository;
    private final ContainerRepository containerRepository;

    //1. 로그인하면 이 코드 호출 하도록
    @Override
    public String createAndStartContainer(String userId) {
        try {
            // 도커 이미지를 통해 컨테이너 생성
            CreateContainerResponse container = dockerClient.createContainerCmd("khv9786/als_ide_jdk")
                    // 사용 후 삭제
                    .withHostConfig(HostConfig.newHostConfig().withAutoRemove(true))
//                    .withCmd("java", "-jar", "your-application.jar")
                    .exec();

            com.als.webIde.domain.entity.Container dbContainer = new com.als.webIde.domain.entity.Container();
            dbContainer.setDockerId(container.getId());
            dbContainer.setMember(memberRepository.findById(Long.parseLong(userId)).orElseThrow(
                    () -> new IllegalArgumentException("Invalid user ID")));
            containerRepository.save(dbContainer);

            // 컨테이너 시작
            dockerClient.startContainerCmd(container.getId()).exec();
            return container.getId();
        } catch (Exception e) {
            log.error("컨테이너 생성에 실패했습니다", e);
            throw new RuntimeException("컨테이너 실행에 실패", e);
        }
    }

    //로그아웃시에는 이 코드 호출
    @Override
    @Transactional
    public void stopAndRemoveContainer(String containerId) {
        try {
            containerRepository.deleteByDockerId(containerId);
            dockerClient.stopContainerCmd(containerId).exec();
//            dockerClient.removeContainerCmd(containerId).exec(); //없어도 컨테이너 제거 되는듯
        } catch (Exception e) {
            log.error("컨테이너 종료 및 제거에 실패했습니다.", e);
            throw new RuntimeException("컨테이너 제거 실패: " + e.getMessage());
        }
    }

    @Override
    public String executeCommand(String containerId, String code, String className, String input) {
        System.out.println("DockerServiceImpl.executeCommand");
        try {
            // Base64 인코딩된 코드를 파일로 저장
            String encodedCode = Base64.getEncoder().encodeToString(code.getBytes());
            // 입력 데이터를 Base64 인코딩하여 파일로 저장
            String encodedInput = Base64.getEncoder().encodeToString(input.getBytes());
            String command = String.format(
                    "mkdir -p /app && echo %s | base64 -d > /app/%s.java && echo %s | base64 -d > /app/input.txt && javac /app/%s.java && java -cp /app %s < /app/input.txt",
                    encodedCode, className, encodedInput, className, className
            );

            ExecCreateCmdResponse execCreateResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            ExecStartResultCallback resultCallback = new ExecStartResultCallback(outputStream, errorStream);

            dockerClient.execStartCmd(execCreateResponse.getId()).exec(resultCallback).awaitCompletion();
            // 오류 스트림의 내용을 검사하고, 오류가 있으면 오류 내용을 반환
            String errorOutput = errorStream.toString();
            if (!errorOutput.isEmpty()) {
                System.out.println("Error Stream Output: " + errorOutput);
                return errorOutput;  // 오류 내용 반환
            }

            // 성공적인 실행 결과 반환
            return outputStream.toString();
        } catch (Exception e) {
            log.error("코드 실행에 실패했습니다.", e);
            return "실행 오류: " + e.getMessage();
        }
    }



    @Override
    public String findContainerByUserId(String userId) {
        Optional<com.als.webIde.domain.entity.Container> dbContainerOpt = containerRepository.findByMemberUserPk(Long.valueOf(userId));
        if (dbContainerOpt.isPresent()) {
            com.als.webIde.domain.entity.Container dbContainer = dbContainerOpt.get();
            // 실제 Docker 환경에서 컨테이너의 존재 여부를 확인
            try {
                dockerClient.inspectContainerCmd(dbContainer.getDockerId()).exec();
                return dbContainer.getDockerId(); // 컨테이너가 존재하는 경우, ID 반환
            } catch (NotFoundException e) {
                // 컨테이너가 존재하지 않는 경우, 새 컨테이너를 생성
                return recreateAndSaveContainer(userId, dbContainer);
            }
        } else {
            // DB에 컨테이너 정보가 없는 경우, 새 컨테이너를 생성
            return createAndStartContainer(String.valueOf(userId));
        }
    }

    @Transactional
    public String recreateAndSaveContainer(String userId, com.als.webIde.domain.entity.Container dbContainer) {
        // 기존 컨테이너 정보 삭제
        containerRepository.deleteById(dbContainer.getContainerPk());
        // 삭제 확인 로직
        if (containerRepository.existsById(dbContainer.getContainerPk())) {
            throw new IllegalStateException("삭제되지 않은 컨테이너 정보가 존재합니다.");
        }
        // 새 컨테이너 생성 및 저장
        return createAndStartContainer(userId);
    }

//        @Override
//        public List<String> listUserContainers (String userId){
//            return dockerClient.listContainersCmd()
//                    .withLabelFilter(Collections.singletonMap("userId", userId))
//                    .exec()
//                    .stream()
//                    .map(Container::getId)
//                    .collect(Collectors.toList());
//        }

        @Override
        public void test () {
            dockerClient.pingCmd().exec();
        }
}