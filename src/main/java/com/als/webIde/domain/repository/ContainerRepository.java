package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container,Long> {

    Long findByMemberUserPk(Long userPk);
}
