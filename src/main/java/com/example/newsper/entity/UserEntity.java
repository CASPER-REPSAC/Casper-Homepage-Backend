package com.example.newsper.entity;

import com.example.newsper.constant.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Entity(name = "userEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "pw", nullable = false)
    private String pw;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "role")
    private UserRole role;

    @Column(name = "introduce")
    private String introduce;

    @Column(name = "refreshtoken")
    private String refreshToken;

    @Column(name = "profileImgPath")
    private String profileImgPath;

    @Column(name = "homepage")
    private String homepage;

    public Map<String, Object> toJSON() {

        Map<String, Object> map = new HashMap<>();

        map.put("role", role.getRole());
        map.put("name", name);
        map.put("nickname", nickname);
        map.put("email", email);
        map.put("introduce", introduce);
        map.put("id", id);
        map.put("image", profileImgPath);
        map.put("homepage", homepage);

        return map;
    }
}
