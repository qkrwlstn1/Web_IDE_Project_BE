package com.als.webIde.DTO.etc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException{
    CustomErrorCode errorCode;
}
