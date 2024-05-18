package com.als.webIde.domain.repository;

import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.domain.entity.MemberSettingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberSettingRepository extends JpaRepository<MemberSetting, MemberSettingId> {
    Optional<MemberSetting> findMemberSettingByNickname(String nickName);
}
