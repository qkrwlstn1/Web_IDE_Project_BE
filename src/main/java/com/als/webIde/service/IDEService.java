package com.als.webIde.service;


import com.als.webIde.DTO.etc.CustomException;
import com.als.webIde.DTO.etc.DTO;
import com.als.webIde.DTO.request.AddFileDto;
import com.als.webIde.DTO.request.CodeExecutionRequestDto;
import com.als.webIde.DTO.request.FileUpdateDto;
import com.als.webIde.DTO.response.CodeExecutionResponseDto;
import com.als.webIde.DTO.response.CodeResponseDto;
import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.FileRepository;
import com.als.webIde.validate.IDEValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IDEService {

    private final FileRepository fileRepository;
    private final IDEValidator ideValidator;
    private final DockerService dockerService;


    //파일 생성
    public ResponseEntity<DTO> createFile(AddFileDto addFileDto) {
        String fileName = addFileDto.getFileName();
        fileName = ideValidator.removeFileSuffix(fileName);
        ideValidator.isValidClassName(fileName);
        ideValidator.checkDuplicateFileName(fileName, addFileDto.getUserPk(), null);

        addFileDto.setFileName(fileName);
        File savedFile = fileRepository.save(addFileDto.toEntity());

        CodeResponseDto codeResponseDto = new CodeResponseDto();
        codeResponseDto.setFileId(savedFile.getFilePk());
        codeResponseDto.setFilename(savedFile.getFileTitle() + "." +savedFile.getSuffixFile());
        codeResponseDto.setCode(savedFile.getContentCd());

        DTO dto = new DTO("성공", codeResponseDto);
        return ResponseEntity.ok(dto);
    }

    //전체 파일리스트 조회
    public ResponseEntity<DTO> getFileList(Long userId) {
        List<File> files = fileRepository.findAllByMember_UserPk(userId);
        FileListResponseDto fileListResponseDto = new FileListResponseDto();
        List<FileListResponseDto.FileResponseDto> fileListMapping = new ArrayList<>();

        if (!files.isEmpty()) {
            for (File f : files) {
                FileListResponseDto.FileResponseDto fileDto = new FileListResponseDto.FileResponseDto();
                fileDto.setFileId(f.getFilePk());
                fileDto.setFileName(f.getFileTitle() + "." + f.getSuffixFile());
                fileListMapping.add(fileDto);
            }
            fileListResponseDto.setFileList(fileListMapping);
        }else{
            // 기본 파일 생성 메서드 호출
            AddFileDto addFileDto = new AddFileDto();
            addFileDto.setUserPk(userId);
            addFileDto.setFileName("Main");
            createFile(addFileDto);
            return getFileList(userId);

        }
        DTO dto = new DTO("성공", fileListResponseDto);
        return ResponseEntity.ok(dto);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<DTO> getCode(Long fileId, Long userId) {
        File correctFile = ideValidator.getCorrectFile(fileId, userId);

        CodeResponseDto codeResponseDto = new CodeResponseDto();
        codeResponseDto.setFileId(fileId);
        codeResponseDto.setFilename(correctFile.getFileTitle() + "." + correctFile.getSuffixFile());
        codeResponseDto.setCode(correctFile.getContentCd());

        DTO dto = new DTO("성공", codeResponseDto);
        return ResponseEntity.ok(dto);
    }

    //파일 수정
    public ResponseEntity<DTO> updateFile(Long userPk, FileUpdateDto updateDto) {
        Long fileId = Long.valueOf(updateDto.getFileId());
        String fileName = updateDto.getFileName();
        String fileCode = updateDto.getFileCode();
        ideValidator.removeFileSuffix(fileName);

        File presentFile = ideValidator.getCorrectFile(fileId, userPk);

        //사용자가 파일을 수정해서 보내면, 파일의 이름은 기존 파일 명이 될 것이고,
        // 파일 코드내의 파일명(ex. class Main)은 그와 상이 할 수 있다.
        // (파일 명만 수정하는 API가 없으므로) 그냥 파일 내에서 파일 명을 바꾸고 수정한다면,
        // 그것에 맞게 파일명이 수정되도록 해야함.
        String ClassName = ideValidator.extractClassName(fileCode); // 코드중 파일명
        ideValidator.isValidClassName(ClassName); //유효한지 검사
        ideValidator.checkDuplicateFileName(ClassName,userPk, fileId);

        //파일명이 바꼈으면, 코드중 클래스명으로 수정.
        if(!Objects.equals(ClassName, fileName)){
            fileName=ClassName;
        }

        presentFile.codeSave(fileName, fileCode); //저장.
        FileUpdateDto fileUpdateDto = new FileUpdateDto();
        fileUpdateDto.setFileId(String.valueOf(fileId));
        fileUpdateDto.setFileName(fileName+".java");
        fileUpdateDto.setFileCode(fileCode);
        DTO dto = new DTO("파일 수정 성공", fileUpdateDto);
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<String> deleteFile(Long filePk, Long userPk) {
        File correctFile = ideValidator.getCorrectFile(filePk, userPk);
        fileRepository.delete(correctFile);
        return ResponseEntity.ok("파일 삭제 성공");
    }

    //코드 실행 요청시 먼저 코드가 저장 -> 실행되도록 해야함.
    public ResponseEntity<DTO> executeCode(CodeExecutionRequestDto codeExecutionRequestDto, Long userPk) {
        System.out.println("ContainerService.executeCode");

        String code = codeExecutionRequestDto.getFileCode();
        String className = ideValidator.extractClassName(code);
        String input = codeExecutionRequestDto.getInput();
        Long filePk = Long.valueOf(codeExecutionRequestDto.getFileId());

        File correctFile = ideValidator.getCorrectFile(filePk, userPk);
        String codeBefore = correctFile.getContentCd();

        if (!Objects.equals(codeBefore, code)) { //코드가 변경됐다면 파일 수정 후 실행
            log.info("파일 변경");
            FileUpdateDto fileUpdateDto = new FileUpdateDto();
            fileUpdateDto.setFileName(className);
            fileUpdateDto.setFileCode(code);
            fileUpdateDto.setFileId(String.valueOf(filePk));
            updateFile(userPk, fileUpdateDto);
        }

        String containerId;
        try {
            containerId = dockerService.findContainerByUserPk(String.valueOf(userPk));
        } catch (CustomException e) { //DB상이나 실제로든 컨테이너 정보가 없다면.
            System.out.println("IDEService.executeCode");
            containerId = dockerService.createAndStartContainer(String.valueOf(userPk));
        }
        log.info("executeCommand 전..");
        // Java 코드 컴파일 및 실행
        String output = dockerService.executeCommand(containerId, code, className, input);
        System.out.println("output = " + output);

        CodeExecutionResponseDto codeExecutionResponseDto = new CodeExecutionResponseDto();
        codeExecutionResponseDto.setResult(output);
        DTO dto = new DTO("성공", codeExecutionResponseDto);

        return ResponseEntity.ok(dto);

    }

}