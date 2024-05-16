package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chatting_message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChattingMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_pk")
    private Long id;

    @Column(name = "message_content", nullable = false, length = 200)
    private String messageContent;

    @Column(name = "message_sendtime", nullable = false)
    private LocalDateTime sendTime;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChattingRoom roomId;

    @ManyToOne
    @JoinColumn(name = "user_pk", nullable = false)
    private Member memberId;

}
