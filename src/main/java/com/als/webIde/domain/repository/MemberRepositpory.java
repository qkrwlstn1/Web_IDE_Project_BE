package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepositpory extends JpaRepository<Member,Long> {
    List<Member> findMemberByUserId(String UserId);
}
