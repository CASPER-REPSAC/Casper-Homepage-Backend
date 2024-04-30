package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.apache.catalina.User;

@Getter
public class JoinDto {

    @Schema(description = "로그인 시 사용할 계정 명")
    private String id;

    @Schema(description = "패스워드 평문")
    private String pw;

    @Schema(description = "이메일 주소 고유 값")
    private String email;

    @Schema(description = "실명")
    private String name;

    @Schema(description = "표시할 별명")
    private String nickname;

    @Schema(description = "프로필 사진 URL")
    private String profileImgPath;

    @Schema(description = "이메일 인증키")
    private String emailKey;

    public UserDto toUserDto(JoinDto dto){
        return new UserDto(dto.getId(),dto.getPw(),dto.getEmail(),dto.getName(),dto.getNickname(),null,null,null,null);
    }
}
