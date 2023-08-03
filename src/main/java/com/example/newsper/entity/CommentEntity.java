package com.example.newsper.entity;

import com.example.newsper.dto.CommentDto;
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
public class CommentEntity {
    @Id
    @Column(name="commentId")
    private Long commentId;

    @Column(name="articleId", nullable = false)
    private Long articleId;

    @Column(name="userId", nullable = false)
    private String userId;

    @Column(name="text", nullable = false)
    private String text;

    @Column(name="createdAt", nullable = false)
    private Date createdAt;

    @Column(name="modifiedAt")
    private Date modifiedAt;

    public static CommentEntity createComment(CommentDto dto, ArticleEntity article) {
        if (dto.getCommentId() != null)
            throw new IllegalArgumentException("댓글 생성 실패!");
        if (dto.getArticleId() != article.getArticleId())
            throw new IllegalArgumentException("댓글 생성 실패!");
        return new CommentEntity(
                dto.getCommentId(),
                dto.getArticleId(),
                dto.getUserId(),
                dto.getText(),
                dto.getCreatedAt(),
                dto.getModifiedAt()
        );
    }

    public void patch(CommentDto dto) {
        if (this.commentId != dto.getCommentId())
            throw new IllegalArgumentException("댓글 수정 실패!");
        if (dto.getUserId() != null){
            this.userId = dto.getUserId();
        }
        if (dto.getText() != null){
            this.text = dto.getText();
        }
    }
}
