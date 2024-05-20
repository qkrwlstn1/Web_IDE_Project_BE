package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_pk")
    private Long filePk;

    @Column(name = "user_pk", nullable = false)
    private Long userPk;

    @Column(name = "suffix_file", nullable = false)
    private String suffixFile;

    @Column(name = "content_cd", columnDefinition = "TEXT")
    private String contentCd;

    @Column(name = "file_title", nullable = false)
    private String fileTitle;

    @Column(name = "path", nullable = false)
    private String path;

    @ManyToOne
    @JoinColumn(name = "user_pk", insertable = false, updatable = false)
    private Member member;

    //    @ManyToOne
//    @JoinColumn(name = "container_pk", insertable = false, updatable = false)
//    private Container container;

    public void codeSave(String fileTitle, String contentCd){
        this.fileTitle = fileTitle;
        this.contentCd = contentCd;
    }

    @Builder
    public File(Long userPk, String fileTitle, String suffixFile,String contentCd, String path) {
        this.userPk = userPk;
        this.fileTitle = fileTitle;
        this.contentCd = contentCd;
        this.suffixFile = suffixFile;
        this.path = path;
    }


}