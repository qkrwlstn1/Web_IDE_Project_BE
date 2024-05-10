package com.als.webIde.controller;

import com.als.webIde.DTO.global.ResponseDto;
import com.als.webIde.DTO.response.CodeResponseDto;
import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.service.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    @Value("${COMPILER_CONTAINER_NAME:compiler}")
    private String compilerContainerName;

    //FileList갱신
    @GetMapping
    public ResponseDto<FileListResponseDto> getFileList(Member member){
        return containerService.getFileList(member.getUserPk());
    }

    //선택한 파일 불러오기
    @GetMapping("/{id}")
    public ResponseDto<CodeResponseDto> getCode(@RequestParam long fileid, Member member){
        return containerService.getCode(fileid);

    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeCode(@RequestParam("file") MultipartFile file,
                                              @RequestParam("input") String input) {
        try {
            // 소스 파일 저장
            String className = saveSourceFile(file);

            // 입력 파일 저장
            saveInputFile(input);

            // Java 코드 컴파일 및 실행
            String output = compileAndRunCode(className);

            return ResponseEntity.ok(output);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error during execution: " + e.getMessage());
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
            throw new IllegalArgumentException("Invalid Java file.");
        }

        File sourceFile = new File("./data/" + fileName);
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
        File inputFile = new File("./data/input.txt");
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
        );
        builder.redirectErrorStream(true);  //첫번쨰 className = 컴파일할 .java 파일 // 두번째 = 실행할 JavaClass이름
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
