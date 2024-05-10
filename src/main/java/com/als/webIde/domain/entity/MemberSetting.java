package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "member_setting")
@AllArgsConstructor
@NoArgsConstructor
public class MemberSetting {

    @EmbeddedId
    private MemberSettingId MemberId;

    @MapsId("member")
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_pk")
    private Member member;

    @Builder.Default
    @Column(name = "Thema", nullable = false)
    private String thema = "white";

    @Column(name = "nickname", nullable = false)
    private String nickname;
}