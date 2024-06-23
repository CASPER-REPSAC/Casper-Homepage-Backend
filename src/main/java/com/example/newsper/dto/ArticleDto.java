package com.example.newsper.dto;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ArticleDto {
    private Long articleId;
    private String userId;
    private String boardId;
    private String category;
    private Date createdAt;
    private Date modifiedAt;
    private String nickname;
    private String title;
    private String content;
    private Boolean hide;
    private Boolean notice;
    private Long view;
    private Long numOfComments;
    private List<String> urls;

    public ArticleEntity toEntity(){
        return new ArticleEntity(articleId,userId,boardId,category,createdAt,modifiedAt,nickname,title,content,hide,notice,view,numOfComments);
    }
}