package com.als.webIde.DTO.response;

import lombok.Data;

@Data
public class CodeResponseDto {
    private Long fileId;
    private String filename;
    private String code;
}
