package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class GoogleDto {

    @Schema(description = "구글 인가코드")
    private String code;

    @Schema(description = "성명")
    private String name;

    @Schema(description = "닉네임")
    private String nickname;
}
