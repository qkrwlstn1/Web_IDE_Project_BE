package com.als.webIde.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class MemberSettingId implements Serializable {

    @Column(name = "user_pk")
    private Long userPk;

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