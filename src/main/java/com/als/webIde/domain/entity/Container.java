package com.als.webIde.domain.entity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "container")
public class Container {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "container_pk")
    private Long containerPk;//컨테이너 아이디

    @Column(name = "docker_id")
    private String dockerId; // Docker 컨테이너 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk")
    private Member member;// 회원 번호

}