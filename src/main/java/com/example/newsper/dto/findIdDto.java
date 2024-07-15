package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class findIdDto {
    @Schema(description = "이메일")
    private String email;
}
