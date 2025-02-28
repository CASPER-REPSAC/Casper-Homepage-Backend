package com.example.newsper.annotations;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("isAuthenticated() && !hasAuthority(T(com.example.newsper.constant.UserRole).ASSOCIATE.toString())")
@SecurityRequirement(name = "Authorization")
// not associate user(regular member) only
public @interface MemberOnly {
}
