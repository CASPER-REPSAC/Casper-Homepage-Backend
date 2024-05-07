package com.example.newsper.entity;

import com.example.newsper.dto.CommentDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Entity(name="commentEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Slf4j
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="commentId")
    private Long commentId;

    @Column(name="articleId", nullable = false)
    private Long articleId;

    @Column(name="text", nullable = false)
    private String text;

    @Column(name="createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "modifiedAt", nullable = false)
    private Date modifiedAt;

    @Column(name = "id", nullable = false)
    private String id;

    @Column(name="nickname", nullable = false)
    private String nickname;

    public static CommentEntity createComment(CommentDto dto, ArticleEntity article) {
        if (dto.getCommentId() != null)
            throw new IllegalArgumentException("댓글 생성 실패!");
        if (dto.getArticleId() != article.getArticleId())
            throw new IllegalArgumentException("댓글 생성 실패!");
        return dto.toEntity();
    }

    public void patch(CommentDto dto) {
        if (this.commentId != dto.getCommentId())
            throw new IllegalArgumentException("댓글 수정 실패!");
        if (dto.getText() != null){
            this.text = dto.getText();
        }
    }
}
