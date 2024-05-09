package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository_re extends JpaRepository<Member,Long> {
    //pk값으로 member 인스턴스 찾기
    Member findByUserPk(Long userPk);
}
