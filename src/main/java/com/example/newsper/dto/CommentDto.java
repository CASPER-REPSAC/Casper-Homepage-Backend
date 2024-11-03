package com.example.newsper.dto;

import com.example.newsper.entity.CommentEntity;
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
    private String text;
    private Date createdAt;
    private Date modifiedAt;
    private String id;
    private String nickname;
    private String profile;

    public static CommentDto createCommentDto(CommentEntity comment) {
        return new CommentDto(
                comment.getCommentId(),
                comment.getArticleId(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.getId(),
                comment.getNickname(),
                null
        );
    }

    public CommentEntity toEntity() {
        return new CommentEntity(commentId, articleId, text, createdAt, modifiedAt, id, nickname);
    }
}
