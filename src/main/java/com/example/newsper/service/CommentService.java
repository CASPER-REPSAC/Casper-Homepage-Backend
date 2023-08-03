package com.example.newsper.service;

import com.example.newsper.dto.CommentDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.repository.ArticleRepository;
import com.example.newsper.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    public List<CommentDto> comments(Long articleId) {
        return commentRepository.findByArticleId(articleId)
                .stream()
                .map(comment -> CommentDto.createCommentDto(comment))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto create(Long articleId, CommentDto dto) {
        ArticleEntity article = articleRepository.findById(articleId).orElseThrow(() -> new IllegalArgumentException("댓글 생성 실패!"));
        CommentEntity comment = CommentEntity.createComment(dto,article);
        CommentEntity created = commentRepository.save(comment);
        return CommentDto.createCommentDto(created);
    }

    @Transactional
    public CommentDto update(Long id, CommentDto dto) {
        CommentEntity target = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("댓글 수정 실패!"));
        target.patch(dto);
        CommentEntity updated = commentRepository.save(target);
        return CommentDto.createCommentDto(updated);
    }

    @Transactional
    public CommentDto delete(Long id) {
        CommentEntity target = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("댓글 삭제 실패!"));
        commentRepository.delete(target);
        return CommentDto.createCommentDto(target);
    }
}
