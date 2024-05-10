package com.als.webIde.DTO.global;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDto<T> {
    private String message;
    private T data;

    public ResponseDto(String message, T data) {
        this.message = message;
        this.data = data;
    }
}
