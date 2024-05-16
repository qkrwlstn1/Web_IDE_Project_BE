package com.als.webIde.service;

import com.als.webIde.DTO.etc.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    public TokenDto login(Long userId,String password) {

        log.info("서비스에 진입 함 userId : {}", userId);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId,password);

        log.info("token의 정보 확인: {}",authenticationToken.getName());

        //Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //log.info("서비스에서 getName() 테스트 : {}",authenticate.toString());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        //token 생성
        String accessToken = tokenProvider.generateAccessToken(authenticationToken);
        String refreshToken = tokenProvider.generateRefreshToken(authenticationToken);

        return TokenDto.builder()
                .grantType("Barer")
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }

    public String reissueAccessToken(String refreshToken) {

        if(tokenProvider.validateToken(refreshToken)){
            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            return tokenProvider.reissueAccessToken(userId);
        }
        return null;
    }
}
