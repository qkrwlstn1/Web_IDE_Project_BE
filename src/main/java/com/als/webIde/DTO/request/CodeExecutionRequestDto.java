package com.als.webIde.DTO.request;

import lombok.Data;

@Data
public class CodeExecutionRequestDto {
    private String fileId;
    private String fileName;
    private String fileCode;
    private String input;
}
