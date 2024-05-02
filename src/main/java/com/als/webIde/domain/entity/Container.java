package com.als.webIde.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Entity
@Table(name = "container")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Container {
    @EmbeddedId
    private ContainerId id;

    @MapsId("userPk")
    @ManyToOne
    @JoinColumn(name = "user_pk")
    private Member member;

    @Column(name = "title")
    private String title;
}

@Embeddable
class ContainerId implements Serializable {
    @Column(name = "container_pk")
    private Long containerPk;

    @Column(name = "user_pk")
    private Long userPk;

    // equals() and hashCode() methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainerId that = (ContainerId) o;
        return Objects.equals(containerPk, that.containerPk) &&
                Objects.equals(userPk, that.userPk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerPk, userPk);
    }
}
