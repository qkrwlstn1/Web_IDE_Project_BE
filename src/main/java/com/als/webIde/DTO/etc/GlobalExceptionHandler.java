package com.als.webIde.DTO.etc;

import com.als.webIde.controller.ContainerController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ContainerController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(NotMyFileException.class)
    protected ResponseEntity<?> NotMyFileException(NotMyFileException e) {
        String message = "내 파일이 아닙니다.";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("message : " + message);
    }

    @ExceptionHandler(FileNameInvalidException.class)
    protected ResponseEntity<?> FileNameInvalidException(FileNameInvalidException e) {
        String message = "파일명은 띄어쓰기 없이 영문자 및 숫자로 20자 이내여야 합니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("message : " + message);
    }
    @ExceptionHandler(DuplicateFileNameException.class)
    protected ResponseEntity<?> DuplicateFileNameException(DuplicateFileNameException e) {
        String message = "이미 존재하는 파일명입니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("message : " + message);
    }

}
