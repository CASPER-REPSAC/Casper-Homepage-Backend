package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RoleDto {

    @Schema(description = "권한 변경할 사용자 계정")
    private String id;

    @Schema(description = "부여할 권한")
    private String role;

}
