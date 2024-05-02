package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class findPwDto {
    @Schema(description = "성명")
    private String name;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "아이디")
    private String id;
}
