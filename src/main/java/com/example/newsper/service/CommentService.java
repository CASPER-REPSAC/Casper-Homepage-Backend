package com.example.newsper.service;

import com.example.newsper.dto.AddCommentDto;
import com.example.newsper.dto.CommentDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.repository.ArticleRepository;
import com.example.newsper.repository.CommentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserService userService;

    @Value("${custom.secret-key}")
    String secretKey;

    public List<CommentDto> comments(Long articleId) {
        return commentRepository.findByArticleId(articleId)
                .stream()
                .map(comment -> CommentDto.createCommentDto(comment))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentEntity create(Long articleId, AddCommentDto _dto, HttpServletRequest request) {
        ArticleEntity article = articleRepository.findById(articleId).orElseThrow(() -> new IllegalArgumentException("댓글 생성 실패!"));
        CommentDto dto = _dto.toCommentDto(_dto);
        dto.setArticleId(articleId);
        Date date = new Date(System.currentTimeMillis()+3600*9*1000);
        dto.setCreatedAt(date);
        dto.setModifiedAt(date);
        String userId = getUserId(request);
        UserEntity userEntity = userService.show(userId);
        dto.setNickname(userEntity.getNickname());
        CommentEntity comment = CommentEntity.createComment(dto,article);
        return commentRepository.save(comment);
    }

    @Transactional
    public CommentEntity update(Long id, CommentDto dto) {
        CommentEntity target = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("댓글 수정 실패!"));
        dto.setCommentId(id);
        target.patch(dto);
        CommentEntity updated = commentRepository.save(target);
        return commentRepository.save(updated);
    }

    @Transactional
    public void delete(Long id) {
        CommentEntity target = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("댓글 삭제 실패!"));
        commentRepository.delete(target);
    }

    private String getUserId(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            return JwtTokenUtil.getLoginId(accessToken, secretKey);
        } catch(Exception e){
            return "guest";
        }
    }
}
