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

    @Column(name = "Thema", nullable = false)
    private String thema;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    public static class builder{
        private Member member;
        private String nickname;

        public builder member(Member member){
            this.member = member;
            return this;
        }

        public builder nickname(String nickname){
            this.nickname = nickname;
            return this;
        }

        public MemberSetting build(){
            return new MemberSetting(new MemberSettingId(member.getUserPk()),member,"white",nickname);
        }
    }


}

@Getter
@Embeddable
@NoArgsConstructor
class MemberSettingId implements Serializable {

    @Column(name = "user_pk")
    private Long userPk;

     MemberSettingId(Long userPk){
        this.userPk=userPk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberSettingId memberId = (MemberSettingId) o;
        return Objects.equals(userPk, memberId.userPk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPk);
    }
}

