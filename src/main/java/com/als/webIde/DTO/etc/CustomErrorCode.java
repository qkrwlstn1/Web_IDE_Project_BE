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
    NOT_SAME_PASSWORD(HttpStatus.BAD_REQUEST,"동일한 비밀번호로 변경할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
