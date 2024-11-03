package com.example.newsper.constant;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin"),
    MEMBER("member"),
    ASSOCIATE("associate"),
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
}
