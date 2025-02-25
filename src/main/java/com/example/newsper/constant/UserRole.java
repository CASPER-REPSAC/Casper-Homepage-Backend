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
    ADMIN("admin"),
    ASSOCIATE("associate"),
    GRADUATE("graduate"),
    REST("rest"),
    ACTIVE("active"),
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
            return UserRole.valueOfRole(groups.get(0));
        }

        return UserRole.GUEST;

    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserRole fromString(String role) {
        return valueOfRole(role);
    }
}
