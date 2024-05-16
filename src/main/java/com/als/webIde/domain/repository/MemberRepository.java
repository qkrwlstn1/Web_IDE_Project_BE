package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> {
    List<Member> findMemberByUserId(String UserId);
}
