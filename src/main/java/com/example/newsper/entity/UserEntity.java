package com.example.newsper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
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

    @Column(name="birthdate", nullable = false)
    private Date birthdate;

    @Column(name="profile")
    private String profile;
}
