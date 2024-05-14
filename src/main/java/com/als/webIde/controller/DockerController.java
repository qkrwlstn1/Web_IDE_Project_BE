package com.als.webIde.controller;

import com.als.webIde.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docker")
public class DockerController {

    private final DockerService dockerService;

    @Autowired
    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<String> createAndStartContainer(@PathVariable String userId) {
        try {
            String containerId = dockerService.createAndStartContainer(userId);
            return ResponseEntity.ok("컨테이너 생성완료, 컨테이너 ID: " + containerId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("컨테이너 생성에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/stop/{containerId}")
    public ResponseEntity<String> stopAndRemoveContainer(@PathVariable String containerId) {
        try {
            dockerService.stopAndRemoveContainer(containerId);
            return ResponseEntity.ok("컨테이너 종료.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("컨테이너 종료 실패: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String test() {
        dockerService.test();
        return "test";
    }
}