package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_pk")
    private Member member;

    @Builder.Default
    @Column(name = "Thema", nullable = false)
    private String thema="white";

    @Column(name = "nickname", nullable = false)
    private String nickname;
}

