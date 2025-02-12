package com.example.newsper.dto;

import com.example.newsper.constant.UserRole;
import com.example.newsper.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Setter
public class UserDto {

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

    private String introduce;
    private String homepage;
    private String role;


    public UserEntity toEntity() {
        return new UserEntity(id, pw, email, name, nickname, UserRole.ASSOCIATE, introduce, null, profileImgPath, homepage);
    }
}

