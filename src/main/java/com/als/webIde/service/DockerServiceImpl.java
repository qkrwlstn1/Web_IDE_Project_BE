package com.als.webIde.service;

import com.als.webIde.DTO.etc.CustomErrorCode;
import com.als.webIde.DTO.etc.CustomException;
import com.als.webIde.domain.entity.Container;
import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.MemberRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;

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

            Container dbContainer = new Container();
            dbContainer.setDockerId(container.getId());
            dbContainer.setMember(memberRepository.findById(Long.parseLong(userId)).orElseThrow(
                    () -> new IllegalArgumentException("유효하지 않은 유저")));
            containerRepository.save(dbContainer);
            containerRepository.flush();
            // 컨테이너 시작
            dockerClient.startContainerCmd(container.getId()).exec();
            return container.getId();
        } catch (Exception e) {
            log.error("컨테이너 생성에 실패했습니다", e);
            throw new CustomException(CustomErrorCode.CONTAINER_CREATE_FAIL);
        }

    }

    //로그아웃시에는 이 코드 호출
    @Override
    @Transactional
    public void stopAndRemoveContainer(String containerId) {
        try {
            containerRepository.deleteByDockerId(containerId);
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("컨테이너 종료 및 제거에 실패했습니다.", e);
            throw new CustomException(CustomErrorCode.CONTAINER_DELETE_FAIL);
        }
    }

    @Override
    public String executeCommand(String containerId, String code, String className, String input) {
        System.out.println("DockerServiceImpl.executeCommand");
        try {
            // Base64 인코딩된 코드, 입력 데이터를 파일로 저장
            String encodedCode = Base64.getEncoder().encodeToString(code.getBytes());
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

    // 코드 실행 전 DB와 도커 데스크탑상 컨테이너가 동일한지, 유지되고 있는지 확인하고,
    //실행하도록 로직 분리
    @Override
    public String findContainerByUserPk(String userPk) {
        Optional<Container> container = containerRepository.findByMemberUserPk(Long.valueOf(userPk));

        if (container.isPresent()) { //DB상에는 도커 컨테이너 정보가 있음.
            Container DBContainer = container.get();
            System.out.println("db상 도커 ID : " + DBContainer.getDockerId());
            String dockerId = DBContainer.getDockerId();
            try {
                // 실제 Docker 환경에서 컨테이너의 존재 여부와 그게 DB상 컨테이너와 동일한지 확인
                dockerClient.inspectContainerCmd(dockerId).exec();
            } catch (NotFoundException e) {
                //DB상에는 있는데, 도커 데스크탑에선 없는 경우.
                // ->DB날리고 재 설정
                System.out.println(" DB상에는 있는데, 도커 데스크탑에선 없는 경우.");
                containerRepository.deleteByDockerIdAndMember_UserPk(DBContainer.getDockerId(), Long.valueOf(userPk));
                containerRepository.flush();
                System.out.println("flush.. 해치웠나?");
                System.out.println();
                throw new CustomException(CustomErrorCode.NO_CONTAINER);
            }
            //DB에도 있고, 실제 컨테이너도 구동중인 경우.
            return DBContainer.getDockerId();
        } else {
            //DB에 정보가 없다면. 컨테이너상 구동중인게 있더라도 해치울 방법이 있나?
            // -> 생성시 유효시간을 정하면 될듯.
            throw new CustomException(CustomErrorCode.NO_CONTAINER);
        }
    }
}

//    @Override
//    public String findContainerByUserPk(String userId) {
//        System.out.println();
//        System.out.println("DockerServiceImpl.findContainerByUserPk");
//        Optional<Container> dbContainerOpt = containerRepository.findByMemberUserPk(Long.valueOf(userId));
//        if (dbContainerOpt.isPresent()) {
//            Container dbContainer = dbContainerOpt.get();
//            System.out.println("db상 도커 ID : " + dbContainer.getDockerId());
//            // 실제 Docker 환경에서 컨테이너의 존재 여부를 확인
//            try {
//                dockerClient.inspectContainerCmd(dbContainer.getDockerId()).exec();
//                return dbContainer.getDockerId(); // 컨테이너가 존재하는 경우, ID 반환
//            } catch (NotFoundException e) {
////                // 컨테이너가 존재하지 않는 경우, 새 컨테이너를 생성
////                return recreateAndSaveContainer(userId, dbContainer);
//                // 컨테이너가 존재하지 않는 경우, 새 컨테이너를 생성하고 DB를 업데이트
//                //                dbContainer.setDockerId(newContainerId);
////                containerRepository.save(dbContainer);
//                return createAndStartContainer(String.valueOf(userId));
//            }
//        } else {
//            // DB에 컨테이너 정보가 없는 경우, 새 컨테이너를 생성
//            return createAndStartContainer(String.valueOf(userId));
//        }
//    }

//    @Transactional
//    public String recreateAndSaveContainer(String userId, Container dbContainer) {
//        // 기존 컨테이너 정보 삭제
//        containerRepository.deleteById(dbContainer.getContainerPk());
//        // 삭제 확인 로직
//        if (containerRepository.existsById(dbContainer.getContainerPk())) {
//            throw new IllegalStateException("삭제되지 않은 컨테이너 정보가 존재합니다.");
//        }
//        // 새 컨테이너 생성 및 저장
//        return createAndStartContainer(userId);
//    }

//        @Override
//        public List<String> listUserContainers (String userId){
//            return dockerClient.listContainersCmd()
//                    .withLabelFilter(Collections.singletonMap("userId", userId))
//                    .exec()
//                    .stream()
//                    .map(Container::getId)
//                    .collect(Collectors.toList());
//        }
//
//        @Override
//        public void test () {
//            dockerClient.pingCmd().exec();
//        }
//}