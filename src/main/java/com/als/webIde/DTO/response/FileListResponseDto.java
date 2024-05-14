package com.als.webIde.DTO.response;


import lombok.*;

import java.util.Map;

@Data
public class FileListResponseDto {
    private Map<Long, String> fileList;
}
