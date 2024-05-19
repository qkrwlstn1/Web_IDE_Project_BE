package com.als.webIde.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

