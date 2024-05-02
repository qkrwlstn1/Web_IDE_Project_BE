package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;


@Getter
@Entity
@Table(name = "chatting")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Chatting {
    @EmbeddedId
    private ChattingId id;

    @MapsId("userPk")
    @ManyToOne
    @JoinColumn(name = "user_pk")
    private Member member;
}


@Embeddable
class ChattingId implements Serializable {
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user_pk")
    private Long userPk;

    // equals() and hashCode() methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChattingId that = (ChattingId) o;
        return Objects.equals(roomId, that.roomId) &&
                Objects.equals(userPk, that.userPk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, userPk);
    }
}
