package com.example.newsper.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j

public class JwtTokenUtil {
    private static final Long expireTime = 1000L * 60 * 60;

    @Value("${custom.secret-key}")
    String secretKey;

    // JWT Token 발급
    public static String createToken(String loginId, String key, long expireTimeMs) {
        // Claim = Jwt Token에 들어갈 정보
        // Claim에 loginId를 넣어 줌으로써 나중에 loginId를 꺼낼 수 있음
        SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes());
        Claims claims = Jwts.claims().add("loginId", loginId).build();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTimeMs))
                .signWith(secretKey)
                .compact();
    }

    public static String createRefreshToken(String loginId, String key, long expireTimeMs) {
        return createToken(loginId, key, expireTimeMs);
    }

    // Claims에서 loginId 꺼내기
    public static String getLoginId(String token, String secretKey) {
        return extractClaims(token, secretKey).get("loginId").toString();
    }

    // 밝급된 Token이 만료 시간이 지났는지 체크
    public static boolean isExpired(String token, String secretKey) {
        try {
            Date expiredDate = extractClaims(token, secretKey).getExpiration();
            // Token의 만료 날짜가 지금보다 이전인지 check
            return expiredDate.before(new Date());
        }
        catch (ExpiredJwtException e) {
            return true;
        }
    }

    // SecretKey를 사용해 Token Parsing
    private static Claims extractClaims(String token, String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}