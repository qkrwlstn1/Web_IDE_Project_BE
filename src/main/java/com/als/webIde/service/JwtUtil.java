package com.als.webIde.service;

import com.als.webIde.DTO.etc.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Getter
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private static String secretKey; //= "b298ce3826d81f25acf1f1aa631637ae21422dd8c1bbc99e50d4d827f7b5bd1350de995baa0ee13255f37d2b39c9d2db4f1b618cc299c8052e6ec7a1c15c1944";

    private static SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static TokenDto createJwt(Long userName, Long expiredMs, Long refreshExpiredMs){
        log.info("secretKey:{}",secretKey);

        String accessToken = Jwts.builder()
                .claim("userId", userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiredMs))
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)
                .compact();
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType("Bearer").build();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("memberId",Long.class);
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        }catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e){
            log.info("Expired JWT Token");
        }
        return false;
    }
}
