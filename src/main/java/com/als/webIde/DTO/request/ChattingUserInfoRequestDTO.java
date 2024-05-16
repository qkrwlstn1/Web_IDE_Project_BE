package com.als.webIde.DTO.request;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChattingUserInfoRequestDTO {
    private Long userId;
    private Long roomId;
}
