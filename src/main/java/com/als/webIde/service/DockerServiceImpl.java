package com.als.webIde.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j // 롬복 Logger
@RequiredArgsConstructor // final 처리
@Service
public class DockerServiceImpl implements DockerService {

    private final DockerClient dockerClient;

    @Override
    public String createAndStartContainer(String userId) {
        try {
            // 도커 이미지를 통해 컨테이너 생성
            CreateContainerResponse container = dockerClient.createContainerCmd("khv9786/als_ide_jdk")
                    // 사용 후 삭제
                    .withHostConfig(HostConfig.newHostConfig().withAutoRemove(true))
//                    .withCmd("java", "-jar", "your-application.jar")
                    .exec();

            // 컨테이너 시작
            dockerClient.startContainerCmd(container.getId()).exec();
            return container.getId();
        } catch (Exception e) {
            log.error("컨테이너 생성에 실패했습니다", e);
            throw new RuntimeException("컨테이너 실행에 실패", e);
        }
    }

    @Override
    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("컨테이너 종료에 실패했습니다.", e);
        }
    }

    @Override
    public String findContainerByUserId(String userId) {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();
            for (Container container : containers) {
                String containerUserId = container.getLabels().get("userId");
                if (userId.equals(containerUserId)) {
                    return container.getId();
                }
            }
        } catch (Exception e) {
            System.err.println(userId + ": 의 컨테이너가 존재하지 않습니다. " + e.getMessage());
            e.printStackTrace();
        }
        return "findContainerByUserId 에러";
    }
        @Override
        public List<String> listUserContainers (String userId){
            return dockerClient.listContainersCmd()
                    .withLabelFilter(Collections.singletonMap("userId", userId))
                    .exec()
                    .stream()
                    .map(Container::getId)
                    .collect(Collectors.toList());
        }

        @Override
        public void test () {
            dockerClient.pingCmd().exec();
        }

        @Override
        // 명령 실행 메서드
        public String executeCommand (String containerId, String command){
            try {
                ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                        // 표준 출력, 오류 연결
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .withCmd("/bin/sh", "-c", command)
                        .exec();

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                     ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {

                    ExecStartResultCallback resultCallback = new ExecStartResultCallback(outputStream, errorStream);
                    dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(resultCallback).awaitCompletion();

                    String output = outputStream.toString();
                    String errors = errorStream.toString();

                    if (!errors.isEmpty()) {
                        throw new RuntimeException("명령 실행 중 오류 발생: " + errors);
                    }

                    return output;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("명령 실행이 중단되었습니다", e);
                } catch (IOException e) {
                    throw new RuntimeException("명령 출력 처리 중 오류 발생", e);
                }
            } catch (DockerException e) {
                throw new RuntimeException("Docker 명령 실행 중 예외 발생", e);
            }

        }
    }