package com.example.newsper.service;

import com.example.newsper.constant.UserRole;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    /**
     * 게시글 검색 (기존 메서드)
     */
    public Page<ArticleEntity> searchArticles(String query, String boardId, String category, String searchType, Pageable pageable) {
        return switch (searchType.toLowerCase()) {
            case "title" -> boardId != null && category != null ?
                    articleRepository.findByTitleContainingAndBoardIdAndCategory(query, boardId, category, pageable) :
                    boardId != null ?
                            articleRepository.findByTitleContainingAndBoardId(query, boardId, pageable) :
                            articleRepository.findByTitleContaining(query, pageable);
            case "content" -> boardId != null && category != null ?
                    articleRepository.findByContentContainingAndBoardIdAndCategory(query, boardId, category, pageable) :
                    boardId != null ?
                            articleRepository.findByContentContainingAndBoardId(query, boardId, pageable) :
                            articleRepository.findByContentContaining(query, pageable);
            default -> boardId != null && category != null ?
                    articleRepository.findByTitleContainingOrContentContainingAndBoardIdAndCategory(
                            query, query, boardId, category, pageable) :
                    boardId != null ?
                            articleRepository.findByTitleContainingOrContentContainingAndBoardId(
                                    query, query, boardId, pageable) :
                            articleRepository.findByTitleContainingOrContentContaining(query, query, pageable);
        };
    }

    /**
     * 권한을 고려한 게시글 검색
     */
    public Page<ArticleEntity> searchArticlesWithPermissions(
            String query,
            String boardId,
            String category,
            String searchType,
            Pageable pageable,
            UserEntity user) {
        List<String> accessibleBoardIds = getAccessibleBoardIds(user);
        if (boardId != null && !accessibleBoardIds.contains(boardId)) {
            return Page.empty(pageable);
        }
        Page<ArticleEntity> results;
        if (boardId != null) {
            results = searchArticles(query, boardId, category, searchType, pageable);
        } else {
            results = searchAcrossAccessibleBoards(
                    query,
                    accessibleBoardIds,
                    category,
                    searchType,
                    pageable);
        }

        return filterHiddenArticles(results, user, pageable);
    }

    /**
     * 접근 가능한 게시판 목록 가져오기
     */
    private List<String> getAccessibleBoardIds(UserEntity user) {
        List<String> accessibleBoards = new ArrayList<>();

        // 모든 사용자가 접근 가능한 게시판
        accessibleBoards.add("freedom_board");
        accessibleBoards.add("notice_board");

        if (user == null || user.getRole() == UserRole.GUEST) {
            return accessibleBoards;
        }

        if (user.getRole() == UserRole.ASSOCIATE) {
            accessibleBoards.add("associate_board");
        } else {
            // 정회원 이상은 모든 게시판 접근 가능
            accessibleBoards.add("associate_board");
            accessibleBoards.add("full_board");
            accessibleBoards.add("graduation_board");
        }

        return accessibleBoards;
    }

    /**
     * 숨김 글 필터링
     */
    private Page<ArticleEntity> filterHiddenArticles(
            Page<ArticleEntity> articles,
            UserEntity user,
            Pageable pageable) {

        List<ArticleEntity> filteredContent = articles.getContent()
                .stream()
                .filter(article -> articleService.isHide(article, user))
                .collect(Collectors.toList());

        return new PageImpl<>(
                filteredContent,
                pageable,
                filteredContent.size()
        );
    }

    /**
     * 여러 게시판에서 검색
     */
    public Page<ArticleEntity> searchAcrossAccessibleBoards(
            String query,
            List<String> boardIds,
            String category,
            String searchType,
            Pageable pageable) {

        return switch (searchType.toLowerCase()) {
            case "title" -> category != null ?
                    articleRepository.findByTitleContainingAndBoardIdInAndCategory(
                            query, boardIds, category, pageable) :
                    articleRepository.findByTitleContainingAndBoardIdIn(
                            query, boardIds, pageable);
            case "content" -> category != null ?
                    articleRepository.findByContentContainingAndBoardIdInAndCategory(
                            query, boardIds, category, pageable) :
                    articleRepository.findByContentContainingAndBoardIdIn(
                            query, boardIds, pageable);
            default -> // 제목+내용
                    category != null ?
                            articleRepository.findByTitleOrContentContainingAndBoardIdInAndCategory(
                                    query, boardIds, category, pageable) :
                            articleRepository.findByTitleOrContentContainingAndBoardIdIn(
                                    query, boardIds, pageable);
        };
    }
}
