package com.als.webIde.domain.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "container")
public class Container {
    @Id
    @Column(name = "container_pk")
    private Long containerPk;

    @Column(name = "user_pk", nullable = false)
    private Long userPk;

    @Column(name = "title")
    private String title;

    @OneToOne
    @JoinColumn(name = "user_pk", insertable = false, updatable = false)
    private Member member;

    // Getters and Setters
}