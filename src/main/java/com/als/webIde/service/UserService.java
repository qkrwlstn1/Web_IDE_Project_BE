package com.als.webIde.service;

import com.als.webIde.DTO.etc.TokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    // 1시간
    private Long expiredMs = 1000 * 60 *60L;

    // 2주일
    private Long refreshMx = 1000 * 60 *60*24*14L;
    public TokenDto login(Long userId) {
        return JwtUtil.createJwt(userId,expiredMs,refreshMx);
    }
}
