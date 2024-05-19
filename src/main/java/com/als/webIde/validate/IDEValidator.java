package com.als.webIde.validate;

import com.als.webIde.DTO.etc.CustomErrorCode;
import com.als.webIde.DTO.etc.CustomException;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.FileRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j // 롬복 Logger
@Component
@RequiredArgsConstructor
public class IDEValidator {

    private final FileRepository fileRepository;


    public String removeFileSuffix(String fileName) {
        if (fileName.contains(".java")) {
            fileName = fileName.replace(".java", "");
        }
        return fileName;
    }

    //파일명은 띄어쓰기 없이 영문자, 숫자로 구성 20자 이내.
    public void isValidClassName(String className) {
        if (!className.matches("[a-zA-Z0-9]{1,20}")) {
            throw new CustomException(CustomErrorCode.INVALID_FILENAME);
        }
    }

    //가지고 있는 파일중 중복되는 파일명이 있는지 확인
    public void checkDuplicateFileName(String fileTitle, Long userPk, @Nullable Long filePk) {
        //파일을 저장, 생성, 수정, 실행 할때
        //파일명이 기존과 중복된게 있는지 확인해야함
        List<File> files = fileRepository.findAllByMember_UserPk(userPk);

        if(filePk ==null){ // 생성 시
            for (File file : files) {
                String fileName = file.getFileTitle();
                if (Objects.equals(fileName, fileTitle)) {
                    throw new CustomException(CustomErrorCode.DUPLICATE_FILENAME);
                }
            }
        }else{ //수정 시
            for (File file : files) {
                String fileName = file.getFileTitle();
                Long filepk = file.getFilePk();
                if (Objects.equals(fileName, fileTitle) && !Objects.equals(filepk, filePk)) {
                    throw new CustomException(CustomErrorCode.DUPLICATE_FILENAME);
                }
            }
        }

    }

    // 코드중 파일 명 추출 매서드
    public String extractClassName(String code) {
        String className = "";
        String[] lines = code.split("\\n");

        for (String line : lines) {
            line = line.trim();
            // 대소문자 구분 없이 "class" 키워드 찾기
            if (line.matches("(?i).*\\bclass\\b.*")) {
                int startIndex = line.toLowerCase().indexOf("class ") + 6;
                int endIndex = line.indexOf("{", startIndex);
                if (endIndex == -1) {
                    endIndex = line.length();
                }
                className = line.substring(startIndex, endIndex).trim();
                break;
            }
        }
        System.out.println("IDEValidator.extractClassName");
        System.out.println("className = " + className);
        return className;
    }

//    public String extractClassName(String code) {
//        String className = "";
//        String[] lines = code.split("\\n");
//
//        for (String line : lines) {
//            line = line.trim();
//            if (line.startsWith("class ") || line.startsWith("Class ")) {
//                int startIndex = line.indexOf("class ") != -1 ? line.indexOf("class ") + 6 : line.indexOf("Class ") + 6;
//                int endIndex = line.indexOf("{", startIndex);
//                if (endIndex == -1) {
//                    endIndex = line.length();
//                }
//                className = line.substring(startIndex, endIndex).trim();
//                break;
//            }
//        }
//        System.out.println("IDEValidator.extractClassName");
//        System.out.println("className = " + className);
//        return className;
//    }

    //요청받은 파일이 유저의 파일이 맞는지 검증하고, 맞는 파일객체를 반환.
    public File getCorrectFile(Long fileId, Long userPk) {
        List<File> files = fileRepository.findAllByMember_UserPk(userPk);
        File correctFile = null;
        for (File file : files) {
            Long filePk = file.getFilePk();
            if (Objects.equals(filePk, fileId)) {
                correctFile = file;
                break;
            }
        }
        if (correctFile != null) {
            return correctFile;
        } else {
            throw new CustomException(CustomErrorCode.NOT_MY_FILE);
        }
    }
}
