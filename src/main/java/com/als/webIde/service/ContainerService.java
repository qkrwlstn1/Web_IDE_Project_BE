package com.als.webIde.service;


import com.als.webIde.DTO.response.CodeExecutionDto;
import com.als.webIde.DTO.response.CodeResponseDto;
import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.FileRepository;
import com.als.webIde.global.DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final FileRepository fileRepository;
    DTO dto;

    @Value("${COMPILER_CONTAINER_NAME:compiler}")
    private String compilerContainerName;


    @Transactional(readOnly = true)
    public ResponseEntity<DTO> getFileList(Long userId) {
        Long containerId = containerRepository.findByMemberUserPk(userId);
        List<File> fileList = fileRepository.findAllByContainerPk(containerId);

        Map<Long, String> fileMap = new HashMap<>();
        FileListResponseDto fileListResponseDto = new FileListResponseDto();

        if (fileList.size() != 0) {
            for (File f : fileList) {
                Long id = f.getFilePk();
                String filename = f.getFileTitle() + "." + f.getSuffixFile();
                fileMap.put(id, filename);
            }
            fileListResponseDto.setFileList(fileMap);
        }else{
            //기본파일 생성 로직??
            fileMap.put(1L,"Main.java"); // 실제로는 생성된 팡닐 PK 가져오는 로직으로 변경
            fileListResponseDto.setFileList(fileMap);
        }
        dto = new DTO("성공", fileListResponseDto);

        return ResponseEntity.ok(dto);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<DTO> getCode(long fileId){
        File file = fileRepository.findByFilePk(fileId);

        CodeResponseDto codeResponseDto = new CodeResponseDto();
        codeResponseDto.setFileId(fileId);
        codeResponseDto.setFilename(file.getFileTitle() + "." +file.getSuffixFile());
        codeResponseDto.setCode(file.getContentCd());

        dto = new DTO("성공", codeResponseDto);
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
            dto = new DTO("성공", codeExecutionDto);

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
        System.out.println("ContainerService.saveSourceFile");
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
        System.out.println("ContainerService.saveInputFile");
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
        System.out.println("ContainerService.compileAndRunCode");
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