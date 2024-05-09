package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberSettingRepository extends JpaRepository<MemberSetting, Long> {
    List<MemberSetting> findMemberSettingByNickname(String nickName);
}
