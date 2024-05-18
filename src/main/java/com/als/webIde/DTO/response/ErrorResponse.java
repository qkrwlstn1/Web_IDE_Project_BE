package com.als.webIde.DTO.response;

import com.als.webIde.DTO.etc.CustomErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@Builder
public class ErrorResponse {
    private String message;

    public static ResponseEntity<ErrorResponse> toResponse(CustomErrorCode e){
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponse.builder()
                        .message(e.getMessage())
                        .build());
    }
}
