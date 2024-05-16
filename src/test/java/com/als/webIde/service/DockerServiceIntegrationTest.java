package com.als.webIde.service;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class DockerServiceIntegrationTest {

    @Container
    public GenericContainer<?> alsIdeJdkContainer = new GenericContainer<>("khv9786/als_ide_jdk:latest").withExposedPorts(8080);

    @Test
    void testContainerStartup() {
        assertTrue(alsIdeJdkContainer.isRunning());
    }
}