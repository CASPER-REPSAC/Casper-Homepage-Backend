package com.example.newsper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ArticleEntity {
    @Id
    @Column(name = "articleId")
    private Long articleId;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "boardId", nullable = false)
    private String boardId;

    @Column(name = "category", nullable = false)
    private Long category;

    @Column(name = "createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "modifiedAt", nullable = false)
    private Date modifiedAt;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "hide", nullable = false)
    private Long hide;

    @Column(name = "notice", nullable = false)
    private Long notice;

    @Column(name = "view", nullable = false)
    private Long view;

    @Column(name = "file", nullable = false)
    private Long file;

}


