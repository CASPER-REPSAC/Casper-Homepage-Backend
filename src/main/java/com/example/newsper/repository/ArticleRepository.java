package com.example.newsper.repository;

import com.example.newsper.entity.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleEntity,Long> {
    @Query(value = "SELECT * FROM article_entity WHERE board_id = :boardId", nativeQuery = true)
    List<ArticleEntity> findByBoardId(@Param("boardId") Long boardId);
}
