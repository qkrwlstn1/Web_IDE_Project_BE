package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.request.AddFileDto;
import com.als.webIde.DTO.request.FileUpdateDto;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.repository.MemberRepositpory;
import com.als.webIde.global.DTO;
import com.als.webIde.service.ContainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;
    private final MemberRepositpory memberRepositpory;


    @GetMapping("/test")
    public String test(){
        return "test!";
    }

    //FileList갱신
    @GetMapping
    public ResponseEntity<DTO> getFileList(){
        Member member = GetMember();
        log.info("member : {}", member.toString());

        return containerService.getFileList(member.getUserPk());
    }

    //선택한 파일 불러오기
    @GetMapping("/{id}")
    public ResponseEntity<DTO> getCode(@RequestParam String Id){
        Member member = GetMember();
        long fileId = Long.parseLong(Id);
        return containerService.getCode(fileId, member.getUserPk());
    }

    // 코드 실행
    @PostMapping("/execute")
    public ResponseEntity<DTO> executeCode(@RequestParam("file") MultipartFile file,
                                           @RequestParam("input") String input) {
        System.out.println("ContainerController.executeCode");
        return containerService.executeCode(file, input);
    }

    //파일 수정
    @PutMapping("/file/{fileId}")
    public ResponseEntity<DTO> fileSave(@RequestBody FileUpdateDto requestDto){
        Long id = Long.valueOf(requestDto.getFileId());
        String fileName = requestDto.getFileName();
        String fileCode = requestDto.getFileCode();
        if(fileName.contains(".java")){
            fileName= fileName.replace(".java","");
        }
        return containerService.saveFile(id,fileName, fileCode);
    }

    @PostMapping("/file")
    public void createFile(@RequestBody AddFileDto dto ){
        Member member = GetMember();
        dto.setUserPk(member.getUserPk());
        System.out.println("dto = " + dto);
        containerService.createFile(dto);
    }

    private Member GetMember() {
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return memberRepositpory.findById(details.getId()).get();
    }

}
