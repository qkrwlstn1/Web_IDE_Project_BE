package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chatting_room")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChattingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_pk")
    private Long id;

    @Column(name = "room_name", length = 20)
    private String roomName;

    @Column(name = "room_date")
    private LocalDateTime roomDate;

    @Column(name = "room_master")
    private Long roomMaster;

}
