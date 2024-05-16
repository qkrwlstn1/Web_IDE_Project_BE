package com.als.webIde.service;


import com.als.webIde.DTO.request.AddFileDto;
import com.als.webIde.DTO.request.FileUpdateDto;
import com.als.webIde.DTO.response.CodeExecutionDto;
import com.als.webIde.DTO.response.CodeResponseDto;
import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.FileRepository;
import com.als.webIde.DTO.etc.DTO;
import com.als.webIde.validate.IDEValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final FileRepository fileRepository;
    private final IDEValidator ideValidator;


    @Value("${COMPILER_CONTAINER_NAME:compiler}")
    private String compilerContainerName;

    //파일 생성
    public ResponseEntity<DTO> createFile(AddFileDto addFileDto) {
        String fileName = addFileDto.getFileName();
        fileName = ideValidator.removeFileSuffix(fileName);
        ideValidator.isValidClassName(fileName);
        ideValidator.checkDuplicateFileName(fileName, addFileDto.getUserPk());

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
//        String beforeFileName = presentFile.getFileTitle(); //이전 파일명.

        //사용자가 파일을 수정해서 보내면, 파일의 이름은 기존 파일 명이 될 것이고,
        // 파일 코드내의 파일명(ex. class Main)은 그와 상이 할 수 있다.
        // (파일 명만 수정하는 API가 없으므로) 그냥 파일 내에서 파일 명을 바꾸고 수정한다면,
        // 그것에 맞게 파일명이 수정되도록 해야함.
        String ClassName = ideValidator.extractClassName(fileCode); // 코드중 파일명
        ideValidator.isValidClassName(ClassName); //유효한지 검사
        ideValidator.checkDuplicateFileName(ClassName,userPk);

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


    //////////////////////////////////////////////////////////////////////////////
    //코드 실행 요청시 먼저 코드가 저장 -> 실행되도록 해야함.
    public ResponseEntity<DTO> executeCode(MultipartFile file, String input){
        System.out.println("ContainerService.executeCode");

        try {
            CodeExecutionDto codeExecutionDto = new CodeExecutionDto();
            // 소스 파일 저장
            String className = saveSourceFile(file);

            // 입력 파일 저장
            saveInputFile(input);

            // Java 코드 컴파일 및 실행
            String output = compileAndRunCode(className);
            codeExecutionDto.setResult(output);
            DTO dto = new DTO("성공", codeExecutionDto);

            return ResponseEntity.ok(dto);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("실패 : "+ e.getMessage());
        }
    }

    /**
     * .java 소스 파일을 Docker 볼륨에 저장.
     * @param file 업로드된 .java 파일
     * @return 파일에서 추출한 클래스 이름
     * @throws IOException 파일 저장 오류
     */
    private String saveSourceFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".java")) {
            throw new IllegalArgumentException("java 파일이 아닙니다. 파일명을 확인해주세요.");
        }

        java.io.File sourceFile = new java.io.File("./data/" + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write(new String(file.getBytes()));
        }
        return fileName.replace(".java", "");
    }

    /**
     * 입력 데이터를 파일에 저장
     * @param input 사용자의 입력 문자열
     * @throws IOException 파일 저장 오류
     */
    private void saveInputFile(String input) throws IOException {
        java.io.File inputFile = new java.io.File("./data/input.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile))) {
            writer.write(input);
            writer.newLine();
        }
    }

    /**
     * Docker 컨테이너를 사용하여 Java 코드를 컴파일하고 실행.
     * @param className 컴파일할 클래스의 이름
     * @return 실행 결과 문자열
     * @throws IOException Docker 실행 오류
     * @throws InterruptedException 프로세스 대기 오류
     */
    private String compileAndRunCode(String className) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "docker", "exec", compilerContainerName,
                "sh", "-c", String.format("javac /app/%s.java && java -cp /app %s < /app/input.txt", className, className)
        );                         //첫번쨰 className = 컴파일할 .java 파일 // 두번째 = 실행할 JavaClass이름
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        }
    }


}