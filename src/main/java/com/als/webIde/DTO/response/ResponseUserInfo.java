package com.als.webIde.DTO.response;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@Builder
public class ResponseUserInfo {
    private String userId;
    private String nickName;
}
