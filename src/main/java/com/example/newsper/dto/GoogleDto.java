package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class GoogleDto {

    @Schema(description = "구글 인가코드")
    private String code;

    @Schema(description = "프론트 리다이렉션 URI")
    private String redirectUri;

}
