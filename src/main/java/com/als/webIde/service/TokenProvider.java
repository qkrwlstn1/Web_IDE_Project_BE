package com.als.webIde.service;

import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;


@Getter
@Slf4j
@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final String AUTHORITIES_KEY = "auth";
    private final long accessTokenValidTime = (60 * 1000) * 3; // 3시간
    private final long refreshTokenValidTime = (60 * 1000) * 60 * 24 * 14; // 7일

    @PostConstruct
    protected void init() {
        // key를 base64로 인코딩
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(encodedKey.getBytes());
    }

    public String generateToken(Authentication authentication, Long time) {
        //CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        // 인증된 사용자의 권한 목록 조회
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + time);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String reissueAccessToken(Long userId){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, accessTokenValidTime);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenValidTime);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) throws ExpiredJwtException {
        Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(authToken);
        return true;

    }
}