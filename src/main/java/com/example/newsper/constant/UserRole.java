package com.example.newsper.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin"), // 관리자
    ASSOCIATE("associate"), // 준회원
    GRADUATE("graduate"), // 졸업생
    REST("rest"), // 비활동
    ACTIVE("active"), // 활동중
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserRole fromString(String role) {
        return valueOfRole(role);
    }
}
