package com.example.newsper.service;

import com.example.newsper.dto.AddCommentDto;
import com.example.newsper.dto.CommentDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.repository.ArticleRepository;
import com.example.newsper.repository.CommentRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Value("${custom.secret-key}")
    String secretKey;

    public List<CommentDto> comments(Long articleId) {
        List<CommentDto> dtos = commentRepository.findByArticleId(articleId)
                .stream()
                .map(comment -> CommentDto.createCommentDto(comment))
                .collect(Collectors.toList());

        for (CommentDto dto : dtos) {
            UserEntity userEntity = userService.findById(dto.getId());
            if(userEntity != null)
                dto.setProfile(userService.findById(dto.getId()).getProfileImgPath());
        }

        return dtos;
    }

    public CommentEntity findById(Long commentId){
        return commentRepository.findById(commentId).orElse(null);
    }

    public void commentCount(Long articleId){
        ArticleEntity articleEntity = articleService.findById(articleId);
        articleEntity.setNumOfComments((long) comments(articleId).size());
        articleService.save(articleEntity);
    }

    @Transactional
    public CommentEntity create(Long articleId, AddCommentDto _dto, HttpServletRequest request) {
        ArticleEntity article = articleRepository.findById(articleId).orElseThrow(() -> new IllegalArgumentException("댓글 생성 실패!"));
        CommentDto dto = _dto.toCommentDto(_dto);
        dto.setArticleId(articleId);
        Date date = new Date(System.currentTimeMillis());
        dto.setCreatedAt(date);
        dto.setModifiedAt(date);
        String userId = getUserId(request);
        UserEntity userEntity = userService.findById(userId);
        dto.setId(userEntity.getId());
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

    public void deleteByArticle(Long articleId){
        List<CommentEntity> comments = commentRepository.findByArticleId(articleId);
        commentRepository.deleteAll(comments);
    }

    private String getUserId(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            return JwtTokenUtil.getLoginId(accessToken, secretKey);
        } catch (Exception e) {
            return "guest";
        }
    }

    public boolean writerCheck(CommentEntity comment, HttpServletRequest request) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        return comment.getId().equals(user.getId()) || user.getRole().equals("admin");
    }

    public boolean authCheck(Long articleId, HttpServletRequest request) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        String boardId = Objects.requireNonNull(articleRepository.findById(articleId).orElse(null)).getBoardId();
        log.info("댓글 권한 체크");

        if(boardId.equals("freedom_board")||boardId.equals("notice_board")) {
            log.info("자유 게시판, 공지사항은 누구나 열람 가능합니다");
            return true;
        }
        else if(user == null) {
            log.info("유저 데이터에 조회할 수 없습니다");
            return false;
        }
        else if(user.getRole().equals("associate")) {
            log.info("준회원은 준회원 게시판 열람이 가능합니다");
            return boardId.equals("associate_board");
        }
        else {
            log.info("정회원은 모든 게시판 열람이 가능합니다");
            return true;
        }
    }
}
