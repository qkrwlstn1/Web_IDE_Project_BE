package com.als.webIde.DTO.request;

import lombok.Data;

@Data
public class FileSaveDto {
    private String fileId;
    private String fileName;
    private String fileCode;
}
