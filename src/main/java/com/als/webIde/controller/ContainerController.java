package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.request.AddFileDto;
import com.als.webIde.DTO.request.FileUpdateDto;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.repository.MemberRepository;
import com.als.webIde.DTO.etc.DTO;
import com.als.webIde.service.ContainerService;
import com.als.webIde.service.DockerServiceImpl;
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
    private final MemberRepository memberRepository;
    private final DockerServiceImpl dockerService;


    //FileList갱신

    @GetMapping
    public ResponseEntity<DTO> getFileList(){
        long memberPk = getMemberPk();
        return containerService.getFileList(memberPk);
    }
    //선택한 파일 불러오기

    @GetMapping("/{id}")
    public ResponseEntity<DTO> getCode(@RequestParam("fileId") String Id){
        long memberPk = getMemberPk();
        long fileId = Long.parseLong(Id);
        return containerService.getCode(fileId, memberPk);
    }
    // 코드 실행

    @PostMapping("/execute")
    public ResponseEntity<DTO> executeCode(@RequestParam("file") MultipartFile file,
                                               @RequestParam("input") String input) {
        long memberPk = getMemberPk();
        System.out.println("ContainerController.executeCode");
        return containerService.executeCode(file, input,memberPk);
    }
    //파일 수정

    @PutMapping("/file/{fileId}")
    public ResponseEntity<DTO> updateFile(@RequestBody FileUpdateDto requestDto){
        long memberPk = getMemberPk();
        return containerService.updateFile(memberPk,requestDto);
    }
    //파일 생성

    @PostMapping("/file")
    public ResponseEntity<DTO> createFile(@RequestBody AddFileDto dto ){
        long memberPk = getMemberPk();
        dto.setUserPk(memberPk);
        return containerService.createFile(dto);
    }
    //파일 삭제

    @DeleteMapping("/file/{filePk}")
    public ResponseEntity<String> deleteFile(@RequestParam("filePk") Long filePk) {
        long memberPk = getMemberPk();
        return containerService.deleteFile(filePk, memberPk);
    }

    //로그아웃시 이요청을 보내게 하던가 아니면, 메서드 호출하도록.
    @DeleteMapping("/container")
    public ResponseEntity<String> stopAndRemoveContainer() {
        long memberPk = getMemberPk();
        String containerId = dockerService.findContainerByUserId(String.valueOf(memberPk));
        dockerService.stopAndRemoveContainer(containerId);
        return ResponseEntity.ok("컨테이너 종료.");

    }

    // 시큐리티 컨텍스트에서 유저의 pk값 가져옴
    private long getMemberPk() {
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberRepository.findById(details.getId()).get();
        return member.getUserPk();
    }
}
