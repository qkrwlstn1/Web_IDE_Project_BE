package com.als.webIde.DTO.response;


import lombok.*;

import java.util.*;

@Data
public class FileListResponseDto {
    private List<FileResponseDto> fileList;

    @Data
    public static class FileResponseDto {
        private Long fileId;
        private String fileName;
    }
}