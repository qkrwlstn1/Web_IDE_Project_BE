package com.als.webIde.service;

import com.als.webIde.DTO.etc.CustomException;
import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.etc.UserInfoDetails;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.domain.entity.MemberSettingId;
import com.als.webIde.domain.repository.MemberRepository;
import com.als.webIde.domain.repository.MemberSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.als.webIde.DTO.etc.CustomErrorCode.ERROR_USER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final MemberSettingRepository memberSettingRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {

        Member member = memberRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new CustomException(ERROR_USER));
        MemberSetting memberSetting = memberSettingRepository.findById(new MemberSettingId(member.getUserPk()))
                .orElseThrow(() -> new CustomException(ERROR_USER));

        UserInfoDetails user = UserInfoDetails.builder().
                id(member.getUserPk())
                .userId(member.getUserId())
                .userPassword(member.getPassword())
                .userNickName(memberSetting.getNickname())
                .userThema(memberSetting.getThema())
                .build();

        return new CustomUserDetails(user);
    }

}
