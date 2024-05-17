package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.etc.DTO;
import com.als.webIde.DTO.request.AddFileDto;
import com.als.webIde.DTO.request.CodeExecutionRequestDto;
import com.als.webIde.DTO.request.FileUpdateDto;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.repository.MemberRepository;
import com.als.webIde.service.DockerServiceImpl;
import com.als.webIde.service.IDEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class IDEController {

    private final IDEService IDEService;
    private final MemberRepository memberRepository;
    private final DockerServiceImpl dockerService;


    //파일 생성
    @PostMapping("/file")
    public ResponseEntity<DTO> createFile(@RequestBody AddFileDto dto ){
        long memberPk = getMemberPk();
        dto.setUserPk(memberPk);
        return IDEService.createFile(dto);
    }

    //FileList갱신
    @GetMapping
    public ResponseEntity<DTO> getFileList(){
        long memberPk = getMemberPk();
        return IDEService.getFileList(memberPk);
    }

    //선택한 파일 불러오기
    @GetMapping("/{id}")
    public ResponseEntity<DTO> getCode(@RequestParam("fileId") String Id){
        long memberPk = getMemberPk();
        long fileId = Long.parseLong(Id);
        return IDEService.getCode(fileId, memberPk);
    }

    // 코드 실행
    @PostMapping("/execute")
    public ResponseEntity<DTO> executeCode(@RequestBody CodeExecutionRequestDto codeExecutionRequestDto) {
        long memberPk = getMemberPk();
        return IDEService.executeCode(codeExecutionRequestDto, memberPk);
    }

    //파일 수정
    @PutMapping("/file/{fileId}")
    public ResponseEntity<DTO> updateFile(@RequestBody FileUpdateDto requestDto){
        long memberPk = getMemberPk();
        return IDEService.updateFile(memberPk,requestDto);
    }
    //파일 삭제

    @DeleteMapping("/file/{filePk}")
    public ResponseEntity<String> deleteFile(@RequestParam("filePk") Long filePk) {
        long memberPk = getMemberPk();
        return IDEService.deleteFile(filePk, memberPk);
    }

    //로그아웃시 이요청을 보내게 하던가 아니면, 메서드 호출하도록.
    @DeleteMapping("/container")
    public ResponseEntity<String> stopAndRemoveContainer() {
        long memberPk = getMemberPk();
        String containerId = dockerService.findContainerByUserPk(String.valueOf(memberPk));
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
