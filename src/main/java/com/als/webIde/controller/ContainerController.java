package com.als.webIde.controller;

import com.als.webIde.DTO.response.CodeExecutionDto;
import com.als.webIde.DTO.response.CodeResponseDto;
import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.global.DTO;
import com.als.webIde.service.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    @GetMapping("/test")
    public String test(){
        return "test!";
    }
    //FileList갱신
    @GetMapping
    public ResponseEntity<DTO> getFileList(Member member){
        return containerService.getFileList(member.getUserPk());
    }

    //선택한 파일 불러오기
    @GetMapping("/{id}")
    public ResponseEntity<DTO> getCode(@RequestParam long fileid, Member member){
        return containerService.getCode(fileid);

    }

    @PostMapping("/execute")
    public ResponseEntity<DTO> executeCode(@RequestParam("file") MultipartFile file,
                                           @RequestParam("input") String input) {
        System.out.println("ContainerController.executeCode");
        return containerService.executeCode(file, input);
    }

}
