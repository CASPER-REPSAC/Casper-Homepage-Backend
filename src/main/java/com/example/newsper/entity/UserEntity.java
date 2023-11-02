package com.example.newsper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name="userEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(name="id")
    private String id;

    @Column(name="pw", nullable = false)
    private String pw;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name="role")
    private String role;

    @Column(name="introduce")
    private String introduce;

    @Column(name="refreshtoken")
    private String refreshToken;

    @Column(name="profileImgPath")
    private String profileImgPath;

    @Column(name="homepage")
    private String homepage;

//    @Column(name="profileImgName")
//    private String profileImgName;
//

}
