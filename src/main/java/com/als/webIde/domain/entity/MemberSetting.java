package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Entity
@Table(name = "member_setting")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSetting {

    @EmbeddedId
    private MemberSettingId MemberId;

    @MapsId("member")
    @OneToOne
    @JoinColumn(name = "user_pk")
    private Member member;

    @Builder.Default
    @Column(name = "Thema", nullable = false)
    private String thema="white";

    @Column(name = "nickname", nullable = false)
    private String nickname;

}

@NoArgsConstructor
@Embeddable
class MemberSettingId implements Serializable {

    private Long member;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberSettingId memberId = (MemberSettingId) o;
        return Objects.equals(member, memberId.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(member);
    }
}

