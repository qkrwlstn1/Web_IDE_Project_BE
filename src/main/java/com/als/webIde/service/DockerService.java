package com.als.webIde.service;

import java.util.List;

public interface DockerService {
    /**
     * 유저에게 도커 컨테이너 생성
     * @param userId
     * @return ContainerId
     */
    String createAndStartContainer(String userId);

    /**
     * 유저 컨테이너 종료 및 삭제
     * @param containerId 종료되는 컨테이너 ID
     */
    void stopAndRemoveContainer(String containerId);
    String findContainerByUserId(String userId);
    List<String> listUserContainers(String userId);
    void test();

    String executeCommand(String containerId, String command);

}