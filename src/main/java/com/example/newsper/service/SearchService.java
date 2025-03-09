package com.example.newsper.service;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    @Autowired
    private ArticleRepository articleRepository;
    /**
     * 게시글 검색
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
}
