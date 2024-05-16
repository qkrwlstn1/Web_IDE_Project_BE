package com.als.webIde.DTO.etc;

import lombok.Data;


@Data
public class DTO<Object> {
    private String message;
    private Object Data;

    public DTO(String message, Object data) {
        this.message = message;
        Data = data;
    }

    public DTO(String message) {
        this.message = message;
    }
}
