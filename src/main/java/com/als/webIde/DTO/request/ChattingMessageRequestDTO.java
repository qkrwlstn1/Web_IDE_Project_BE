package com.als.webIde.DTO.request;

import lombok.Data;

@Data
public class ChattingMessageRequestDTO {
    private Long chattingRoomId;
    private Long memberId;
    private String messageContent;
}
