package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "member_setting")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSetting {
    @Id
    @OneToOne
    @JoinColumn(name = "member_pk")
    private Member member;

    @Column(name = "Thema", nullable = false)
    private String thema;

    @Column(name = "nickname", nullable = false)
    private String nickname;

}
