package com.example.newsper.service;

import com.example.newsper.constant.UserRole;
import com.example.newsper.dto.ArticleDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import com.example.newsper.entity.BoardEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FileService fileService;

    public List<ArticleList> boardList(String boardId, String category, Long listNum) {
        if (category.equals("all")) return articleRepository.findByBoardListAll(boardId, listNum);
        else return articleRepository.findByBoardList(boardId, category, listNum);
    }

    public int getMaxPageNum(String boardId, String category) {
        if (category.equals("all")) return articleRepository.findAllBoardListCount2(boardId);
        return articleRepository.findAllBoardListCount(boardId, category);
    }

    public ArticleEntity findById(Long articleId) {
        return articleRepository.findById(articleId).orElse(null);
    }

    public List<ArticleEntity> findByBoardName(BoardEntity boardEntity) {
        return articleRepository.findByBoardName(boardEntity.getBoardNameKey().getBoardName(), boardEntity.getBoardNameKey().getSubBoardName());
    }

    public void delete(ArticleEntity articleEntity) {
        List<String> urls = fileService.getUrls(String.valueOf(articleEntity.getArticleId()), "article");

        for (String url : urls) {
            fileService.delete(url);
        }

        articleRepository.delete(articleEntity);
    }

    public ArticleEntity update(Long id, ArticleDto dto) {
        ArticleEntity target = articleRepository.findById(id).orElse(null);

        if (target == null) {
            log.info("target is null");
            return null;
        }

        if (dto.getTitle() != null) {
            target.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            target.setContent(dto.getContent());
        }

        ArticleEntity updated = articleRepository.save(target);
        log.info(updated.toString());
        return updated;
    }

    public boolean writerCheck(ArticleEntity article, UserEntity user) {
        return article.getUserId().equals(user.getId()) || user.getRole() == UserRole.ADMIN;
    }

    public boolean isHide(ArticleEntity article, UserEntity user) {
        if (!article.getHide()) return true;

        if (user == null) return false;
        else if (user.getRole() == UserRole.ASSOCIATE) return writerCheck(article, user);
        else return true;
    }

    public boolean authCheck(String boardId, UserEntity user) {
        log.info("게시판 권한 체크");
        if (boardId.equals("freedom_board") || boardId.equals("notice_board")) {
            log.info("자유 게시판, 공지사항은 누구나 열람 가능합니다");
            return true;
        } else if (user == null) {
            log.info("유저 데이터에 조회할 수 없습니다");
            return false;
        } else if (user.getRole() == UserRole.ASSOCIATE) {
            log.info("준회원은 준회원 게시판 열람이 가능합니다");
            return boardId.equals("associate_board");
        } else {
            log.info("정회원은 모든 게시판 열람이 가능합니다");
            return true;
        }
    }

    public ArticleEntity save(ArticleEntity articleEntity) {
        return articleRepository.save(articleEntity);
    }

    public ArticleEntity write(ArticleDto dto, UserEntity user) {
        dto.setUserId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setView(0L);
        dto.setNumOfComments(0L);
        Date date = new Date(System.currentTimeMillis());
        dto.setCreatedAt(date);
        dto.setModifiedAt(date);

        ArticleEntity article = dto.toEntity();

        return articleRepository.save(article);
    }
}
