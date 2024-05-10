package com.als.webIde.DTO.response;

import com.als.webIde.domain.entity.ChattingMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChattingMessageResponseDTO {
    private Long messageId;
    private Long memberId;
    private LocalDateTime sendTime;
    private String messageContent;
    private String nickname;

    public ChattingMessageResponseDTO(ChattingMessage msg) {
        this.messageId = msg.getId();
        this.memberId = msg.getMemberId().getUserPk();
        this.sendTime = msg.getSendTime();
        this.messageContent = msg.getMessageContent();
    }
}
