package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {
    // Member 엔티티의 userPk 필드를 기준으로 Container 검색
//    Container findByUserPk_UserPk(Long userPk);
    Optional<Container> findByMemberUserPk(Long userPk); // 메소드 명 변경

    void deleteByDockerId(String containerId);
}