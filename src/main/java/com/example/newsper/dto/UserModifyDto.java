package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserModifyDto {
    @Schema(description = "대상 사용자 ID")
    private String id;

    @Schema(description = "수정 될 사용자 이름")
    private String name;

    @Schema(description = "수정 될 닉네임")
    private String nickname;

    @Schema(description = "수정 될 홈페이지 주소")
    private String homepage;

    @Schema(description = "수정 될 소개 글")
    private String introduce;

    @Schema(description = "수정 될 프로필 URL")
    private String profileImgPath;
}
