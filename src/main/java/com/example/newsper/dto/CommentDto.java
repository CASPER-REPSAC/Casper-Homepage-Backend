package com.example.newsper.dto;

import com.example.newsper.entity.CommentEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class CommentDto {
    private Long commentId;
    private Long articleId;
    private String nickname;
    private String text;
    private Date createdAt;
    private Date modifiedAt;

    public CommentEntity toEntity(){
        return new CommentEntity(commentId,articleId,nickname,text,createdAt,modifiedAt);
    }

    public static CommentDto createCommentDto(CommentEntity comment) {
        return new CommentDto(
                comment.getCommentId(),
                comment.getArticleId(),
                comment.getNickname(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
