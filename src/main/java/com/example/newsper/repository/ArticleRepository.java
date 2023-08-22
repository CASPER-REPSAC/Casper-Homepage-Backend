package com.example.newsper.repository;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleEntity,Long> {
    @Query(value = "SELECT article_id, title, nickname, created_at, view FROM article_entity WHERE board_id = :boardId and category = :category ORDER BY article_id DESC LIMIT :listNum, 10", nativeQuery = true)
    List<ArticleMapping> findByBoardList(@Param("boardId") String boardId, @Param("category") Long category, @Param("listNum") Long listNum);

}