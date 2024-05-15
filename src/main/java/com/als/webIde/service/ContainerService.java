package com.als.webIde.service;



import com.als.webIde.DTO.etc.FileNameInvalidException;
import com.als.webIde.DTO.etc.NotMyFileException;

import com.als.webIde.DTO.request.AddFileDto;
import com.als.webIde.DTO.request.FileUpdateDto;
import com.als.webIde.DTO.response.CodeExecutionDto;
import com.als.webIde.DTO.response.CodeResponseDto;
import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.FileRepository;

import com.als.webIde.DTO.etc.DTO;

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


    @Value("${COMPILER_CONTAINER_NAME:compiler}")
    private String compilerContainerName;


    public ResponseEntity<DTO> getFileList(Long userId) {
        List<File> files = fileRepository.findAllByMember_UserPk(userId);

        Map<Long, String> fileMap = new HashMap<>();
        FileListResponseDto fileListResponseDto = new FileListResponseDto();

        if (files.size() != 0) {
            for (File f : files) {
                Long id = f.getFilePk();
                String filename = f.getFileTitle() + "." + f.getSuffixFile();
                fileMap.put(id, filename);
            }
            fileListResponseDto.setFileList(fileMap);
        }else{

            // 기본 파일 생성 메서드 호출
            AddFileDto addFileDto = new AddFileDto();
            addFileDto.setUserPk(userId);
            addFileDto.setFileName("Main");
            createFile(addFileDto);
            return getFileList(userId);
//            fileMap.put( ,"Main.java"); // 실제로는 생성된 팡닐 PK 가져오는 로직으로 변경
//            fileListResponseDto.setFileList(fileMap);

        }
        DTO dto = new DTO("성공", fileListResponseDto);

        return ResponseEntity.ok(dto);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<DTO> getCode(Long fileId, Long userId) {
        File correctFile = getCorrectFile(fileId, userId);

        CodeResponseDto codeResponseDto = new CodeResponseDto();
        codeResponseDto.setFileId(fileId);
        codeResponseDto.setFilename(correctFile.getFileTitle() + "." + correctFile.getSuffixFile());
        codeResponseDto.setCode(correctFile.getContentCd());

        DTO dto = new DTO("성공", codeResponseDto);
        return ResponseEntity.ok(dto);

    }

    public ResponseEntity<DTO> createFile(AddFileDto addFileDto) {
        String fileName = addFileDto.getFileName();
        if(fileName.contains(".java")){
            fileName= fileName.replace(".java","");
        }

        if(!isValidClassName(fileName)){
            throw new FileNameInvalidException();
        }

        addFileDto.setFileName(fileName);
        File savedFile = fileRepository.save(addFileDto.toEntity());

        CodeResponseDto codeResponseDto = new CodeResponseDto();
        codeResponseDto.setFileId(savedFile.getFilePk());
        codeResponseDto.setFilename(savedFile.getFileTitle() + "." +savedFile.getSuffixFile());
        codeResponseDto.setCode(savedFile.getContentCd());

        DTO dto = new DTO("성공", codeResponseDto);
        return ResponseEntity.ok(dto);
    }

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

    //파일 수정
    public ResponseEntity<DTO> updateFile(Long userPk, FileUpdateDto updateDto) {
        Long fileId = Long.valueOf(updateDto.getFileId());
        String fileName = updateDto.getFileName();
        String fileCode = updateDto.getFileCode();

        //파일 Pk를 사용해 해당 유저의 파일이 맞는지 확인.
        File presentFile = getCorrectFile(fileId, userPk);
        String beforeFileName = presentFile.getFileTitle();

        if (fileName.contains(".java")) {
            fileName = fileName.replace(".java", "");
        }

        //사용자가 파일을 수정해서 보내면, 파일의 이름은 기존 파일 명이 될 것이고,
        // 파일 코드내의 파일명(ex. class Main)은 그와 상이 할 수 있다.
        // (파일 명만 수정하는 API가 없으므로) 그냥 파일 내에서 파일 명을 바꾸고 수정한다면,
        // 그것에 맞게 파일명이 수정되도록 해야함.
        String ClassName = extractClassName(fileCode);

        if(!Objects.equals(ClassName, fileName)){
            fileName=ClassName;
        }

        presentFile.codeSave(fileName, fileCode);
        FileUpdateDto fileUpdateDto = new FileUpdateDto();
        fileUpdateDto.setFileId(String.valueOf(fileId));
        fileUpdateDto.setFileName(fileName+".java");
        fileUpdateDto.setFileCode(fileCode);
        DTO dto = new DTO("파일 수정 성공", fileUpdateDto);
        return ResponseEntity.ok(dto);

    }
    // 코드중 파일 명 추출 매서드
    private String extractClassName(String code) {
        String className = "";
        String[] lines = code.split("\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("class ") || line.startsWith("Class ")) {
                int startIndex = line.indexOf("class ") != -1 ? line.indexOf("class ") + 6 : line.indexOf("Class ") + 6;
                int endIndex = line.indexOf("{", startIndex);
                if (endIndex == -1) {
                    endIndex = line.length();
                }
                className = line.substring(startIndex, endIndex).trim();
                break;
            }
        }

        System.out.println("className = " + className);
        if(!isValidClassName(className)){
            throw new FileNameInvalidException();
        }
        return className;
    }

    //파일명은 띄어쓰기 없이 영문자, 숫자로 구성 20자 이내.
    private boolean isValidClassName(String className) {
        return className.matches("[a-zA-Z0-9]{1,20}");
    }

    public ResponseEntity<String> deleteFile(Long filePk, Long memberPk) {
        File correctFile = getCorrectFile(filePk, memberPk);
        fileRepository.delete(correctFile);
        return ResponseEntity.ok("파일 삭제 성공");
    }


    //요청받은 파일이 유저의 파일이 맞는지 검증하고, 맞는 파일객체를 반환.
    private File getCorrectFile(Long fileId, Long userPk) {
        List<File> files = fileRepository.findAllByMember_UserPk(userPk);
        File correctFile = null;
        for (File file : files) {
            Long filePk = file.getFilePk();
            if(Objects.equals(filePk, fileId)){
                correctFile = file;
                break;
            }
        }
        if(correctFile!=null){
            return correctFile;
        }else{
            throw new NotMyFileException();
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