package com.example.newsper.dto;

import com.example.newsper.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@ToString
public class UserDto {

    private String id;
    private String pw;
    private String email;
    private String name;
    private String nickname;
    private Date birthdate;
    private String profile;

    public UserEntity toEntity() {
        return new UserEntity(id,pw,email,name,nickname,birthdate,profile);
    }
}
