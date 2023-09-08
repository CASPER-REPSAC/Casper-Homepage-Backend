package com.example.newsper.dto;

import com.example.newsper.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@AllArgsConstructor
@ToString
@Setter
public class UserDto {

    private String id;
    private String pw;
    private String email;
    private String name;
    private String nickname;


    public UserEntity toEntity() {
        return new UserEntity(id,pw,email,name,nickname,"준회원");
    }
}
