package com.example.newsper.dto;

import com.example.newsper.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@AllArgsConstructor
@ToString
public class UserDto {

    private String id;
    private String pw;
    private String email;
    private String name;
    private String nickname;
    @DateTimeFormat(pattern = "yyyyMMdd")
    private Date birthdate;


    public UserEntity toEntity() {
        return new UserEntity(id,pw,email,name,nickname,birthdate,"정회원",null,null);
    }
}
