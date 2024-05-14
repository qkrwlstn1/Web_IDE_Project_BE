package com.als.webIde.domain.entity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "container")
public class Container {
    @Id
    @Column(name = "container_pk")
    private Long containerPk;//컨테이너 아이디

    @OneToOne
    @JoinColumn(name = "user_pk", insertable = false, updatable = false)
    private Member userPk;// 회원 번호

}