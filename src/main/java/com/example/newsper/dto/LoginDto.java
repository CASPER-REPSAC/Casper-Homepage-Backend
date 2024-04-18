package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LoginDto {
    @Schema(description = "유저 ID")
    private String id;

    @Schema(description = "유저 PW")
    private String pw;
}
