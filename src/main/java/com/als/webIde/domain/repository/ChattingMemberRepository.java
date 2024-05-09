package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.ChattingMember;
import com.als.webIde.domain.entity.ChattingRoom;
import com.als.webIde.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingMemberRepository extends JpaRepository<ChattingMember,Long> {

    void deleteByChattingRoomAndMember(ChattingRoom cr, Member member);
}
