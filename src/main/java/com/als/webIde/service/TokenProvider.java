package com.als.webIde.service;

import com.als.webIde.DTO.etc.CustomUserDetails;
import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;


import org.springframework.security.core.Authentication;

@Slf4j
@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final String AUTHORITIES_KEY = "auth";
    private final long accessTokenValidTime = (60 * 1000) * 60 * 3; // 30분
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
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String reissueAccessToken(Long userId){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
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

//    public Authentication getAuthentication(String token) {
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//
//        Collection<? extends GrantedAuthority> authorities =
//                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList());
//
//        CustomUserDetails principal = new CustomUserDetails(claims.getSubject(), "", authorities);
//        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
//    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) throws ExpiredJwtException {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // JWT의 서명이 유효하지 않음
            log.info("JWT의 서명이 유효하지 않음");
        } catch (MalformedJwtException ex) {
            log.info("JWT가 올바르게 구성되지 않았습니다.");
            // JWT가 올바르게 구성되지 않음
        } catch (ExpiredJwtException ex) {
            // JWT가 만료됨
            log.info("JWT가 만료됨");
        } catch (UnsupportedJwtException ex) {
            // 지원되지 않는 JWT
            log.info("지원되지 않는 JWT");
        } catch (IllegalArgumentException ex) {
            // JWT의 클레임이 null 또는 비어 있음
            log.info("JWT의 클래엠이 null 또는 비어 있음");
        }
        return false;
    }
}