package com.example.newsper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name="articleEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "articleId")
    private Long articleId;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "boardId", nullable = false)
    private String boardId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "modifiedAt", nullable = false)
    private Date modifiedAt;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "hide", nullable = false)
    private Boolean hide;

    @Column(name = "notice", nullable = false)
    private Boolean notice;

    @Column(name = "view", nullable = false)
    private Long view;

    @Column(name = "numOfComments", nullable = false)
    private Long numOfComments;

    public Map<String,Object> addAuthorInfo(String profile, String introduce){
        Map<String,Object> map = new java.util.HashMap<>();
        map.put("articleId",articleId);
        map.put("userId",userId);
        map.put("profile",profile);
        map.put("introduce",introduce);
        map.put("boardId",boardId);
        map.put("category",category);
        map.put("createdAt",createdAt);
        map.put("modifiedAt",modifiedAt);
        map.put("nickname",nickname);
        map.put("title",title);
        map.put("content",content);
        map.put("hide",hide);
        map.put("notice",notice);
        map.put("view",view);
        map.put("numOfComments",numOfComments);

        return map;
    }
}



