package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chatting_member")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChattingMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatting_member_pk")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_pk", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "room_pk", nullable = false)
    private ChattingRoom chattingRoom;
}
