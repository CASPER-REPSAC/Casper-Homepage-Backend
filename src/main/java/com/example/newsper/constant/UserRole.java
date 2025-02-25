package com.example.newsper.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.List;

@Getter
public enum UserRole {
    ADMIN("admin"),         // 관리자
    ASSOCIATE("associate"), // 준회원
    GRADUATE("graduate"),   // 졸업생
    REST("rest"),           // 비활동
    ACTIVE("active"),       // 활동중
    // Guest must be treated as an unknown user or all users.
    GUEST("guest");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public static UserRole valueOfRole(String role) {
        for (UserRole userRole : values()) {
            if (userRole.getRole().equals(role)) {
                return userRole;
            }
        }
        return GUEST;
    }

    public static UserRole extraUserRoleFromToken(String Token, String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(Token)
                .getPayload();  // 맞으면 payload 부분(groups 포함)을 들고옴

        List<String> groups = claims.get("groups", List.class);

        if (groups != null && !groups.isEmpty()) {
            return switch (groups.getFirst()) {
                case "활동중" -> UserRole.valueOfRole("active");
                case "비활동" -> UserRole.valueOfRole("rest");
//                case "졸업생" :
//                    return UserRole.GRADUATE;
//                case "관리자" :
//                    return UserRole.ADMIN;
//                case "준회원":
//                case "정회원" :
//                    return UserRole.ASSOCIATE;
                default -> UserRole.valueOfRole("guest");
            };
        }
        return UserRole.valueOfRole("guest");
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserRole fromString(String role) {
        return valueOfRole(role);
    }
}
