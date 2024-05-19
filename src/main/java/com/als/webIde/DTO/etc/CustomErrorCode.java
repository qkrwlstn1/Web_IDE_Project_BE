package com.als.webIde.DTO.etc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    CustomErrorCode(HttpStatus.BAD_REQUEST,"이미 존재하는 ID입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST,"이미 존재하는 닉네임입니다."),
    UNKNOWN_PASSWORD(HttpStatus.BAD_REQUEST ,"기존 비밀번호를 확인해주세요."),
    ERROR_PASSWORD(HttpStatus.BAD_REQUEST,"비밀번호가 일치하지 않습니다."),
    ERROR_USER(HttpStatus.BAD_REQUEST,"해당하는 유저가 없습니다."),
    NOT_SAME_PASSWORD(HttpStatus.BAD_REQUEST,"동일한 비밀번호로 변경할 수 없습니다."),
    INVALID_USERID(HttpStatus.BAD_REQUEST,"이미 존재하는 아이디입니다"),


    //IDE
    NOT_MY_FILE(HttpStatus.BAD_REQUEST, "파일이 없습니다."),
    INVALID_FILENAME(HttpStatus.BAD_REQUEST, "파일명은 띄어쓰기 없이 영문자 및 숫자로 20자 이내여야 합니다."),
    DUPLICATE_FILENAME(HttpStatus.BAD_REQUEST,"이미 존재하는 파일명입니다."),
    CONTAINER_CREATE_FAIL(HttpStatus.BAD_REQUEST,"컨테이너 생성에 실패했습니다."),
    CONTAINER_DELETE_FAIL(HttpStatus.BAD_REQUEST,"컨테이너 종료 및 제거에 실패했습니다."),
    NO_CONTAINER(HttpStatus.BAD_REQUEST,"컨테이너 정보가 없습니다.");



    private final HttpStatus httpStatus;
    private final String message;
}
