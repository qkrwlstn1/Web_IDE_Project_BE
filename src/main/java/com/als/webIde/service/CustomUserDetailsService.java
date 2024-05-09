package com.als.webIde.service;

import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.request.UserLogin;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.repository.MemberRepositpory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EmptyStackException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepositpory memberRepositpory;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {

        Member member = memberRepositpory.findById(Long.parseLong(id))
                .orElseThrow(() -> new IllegalArgumentException("해당하는 유저가 없습니다"));

        UserLogin user = UserLogin.builder().userId(member.getUserId())
                .password(member.getPassword()).build();


        return new CustomUserDetails(user);
    }

}
