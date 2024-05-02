package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Entity
@Table(name = "file")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File {
    @EmbeddedId
    private FileId id;

    @MapsId("directoryPk")
    @ManyToOne
    @JoinColumn(name = "directory_pk")
    private Directory directory;

    @Column(name = "content_cd")
    private String contentCd;

    @Column(name = "file_title", nullable = false)
    private String fileTitle;

    @Column(name = "suffix_file", nullable = false)
    private String suffixFile;
}

@Embeddable
class FileId implements Serializable {
    @Column(name = "file_pk")
    private Long filePk;

    @Column(name = "directory_pk")
    private Long directoryPk;

    // equals() and hashCode() methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileId that = (FileId) o;
        return Objects.equals(filePk, that.filePk) &&
                Objects.equals(directoryPk, that.directoryPk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePk, directoryPk);
    }
}
