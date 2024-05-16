package com.als.webIde.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Mockito 확장을 사용하여 Mockito 설정 및 해제를 자동으로 처리.
@ExtendWith(MockitoExtension.class)
class DockerServiceImplTest {
    @Mock
    private DockerClient dockerClient;
    @InjectMocks
    private DockerServiceImpl dockerService;

    @Mock
    private CreateContainerCmd createContainerCmd;
    @Mock
    private StartContainerCmd startContainerCmd;
    @Mock
    private StopContainerCmd stopContainerCmd;
    @Mock
    private RemoveContainerCmd removeContainerCmd;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testCreateAndStartContainer() {
        when(dockerClient.createContainerCmd("khv9786/als_ide_jdk")).thenReturn(createContainerCmd);
        when(createContainerCmd.withHostConfig(any(HostConfig.class))).thenReturn(createContainerCmd);
//        when(createContainerCmd.withCmd("java", "-jar", "your-application.jar")).thenReturn(createContainerCmd);

        CreateContainerResponse response = new CreateContainerResponse();
        response.setId("container123");
        when(createContainerCmd.exec()).thenReturn(response);

        when(dockerClient.startContainerCmd("container123")).thenReturn(startContainerCmd);
        doNothing().when(startContainerCmd).exec();

        String containerId = dockerService.createAndStartContainer("user123");
        assertEquals("container123", containerId);

        verify(createContainerCmd).withHostConfig(any(HostConfig.class));
//        verify(createContainerCmd).withCmd("java", "-jar", "your-application.jar");
        verify(createContainerCmd).exec();
        verify(dockerClient).startContainerCmd("container123");
        verify(startContainerCmd).exec();
    }

    @Test
    void testStopAndRemoveContainer() {
        StopContainerCmd stopContainerCmd = mock(StopContainerCmd.class);
        when(dockerClient.stopContainerCmd("container123")).thenReturn(stopContainerCmd);
        doNothing().when(stopContainerCmd).exec();

        RemoveContainerCmd removeContainerCmd = mock(RemoveContainerCmd.class);
        when(dockerClient.removeContainerCmd("container123")).thenReturn(removeContainerCmd);
        doNothing().when(removeContainerCmd).exec();

        dockerService.stopAndRemoveContainer("container123");

        verify(dockerClient).stopContainerCmd("container123");
        verify(stopContainerCmd).exec();
        verify(dockerClient).removeContainerCmd("container123");
        verify(removeContainerCmd).exec();
    }
}