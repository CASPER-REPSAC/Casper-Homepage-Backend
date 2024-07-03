package com.example.newsper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ArticleListDto {
    private Date createdAt;
    private String title;
    private String nickname;
    private Long articleId;
    private Long view;
    private Boolean file;
    private Boolean hide;
    private Long numOfComments;
}
