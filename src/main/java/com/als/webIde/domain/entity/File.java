package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "file")
public class File {
    @Id
    @Column(name = "file_pk")
    private Long filePk;

    @Column(name = "container_pk", nullable = false)
    private Long containerPk;

    @Column(name = "suffix_file", nullable = false)
    private String suffixFile;

    @Column(name = "content_cd")
    private String contentCd;

    @Column(name = "file_title", nullable = false)
    private String fileTitle;

    @Column(name = "path", nullable = false)
    private String path;

    @ManyToOne
    @JoinColumn(name = "container_pk", insertable = false, updatable = false)
    private Container container;

    // Getters and Setters
}