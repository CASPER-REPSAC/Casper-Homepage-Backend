package com.example.newsper.dto;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ArticleDto {
    private Long articleId;
    private String userId;
    private Long boardId;
    private Long category;
    private Date createdAt;
    private Date modifiedAt;
    private String title;
    private Long hide;
    private Long notice;
    private Long view;
    private Long file;

    public ArticleEntity toEntity(){
        return new ArticleEntity(articleId,userId,boardId,category,createdAt,modifiedAt,title,hide,notice,view,file);
    }
}
