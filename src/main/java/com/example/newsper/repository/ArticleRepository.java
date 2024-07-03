package com.example.newsper.repository;

import com.example.newsper.dto.ArticleListDto;
import com.example.newsper.entity.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleEntity,Long> {
    @Query(value = "SELECT articleId, boardId, hide, numOfComments, title, nickname, createdAt, view FROM articleEntity WHERE boardId = :boardId and category = :category ORDER BY articleId DESC LIMIT :listNum, 10", nativeQuery = true)
    List<ArticleListDto> findByBoardList(@Param("boardId") String boardId, @Param("category") String category, @Param("listNum") Long listNum);

    @Query(value = "SELECT articleId, boardId, hide, numOfComments, title, nickname, createdAt, view FROM articleEntity WHERE boardId = :boardId ORDER BY articleId DESC LIMIT :listNum, 10", nativeQuery = true)
    List<ArticleListDto> findByBoardListAll(@Param("boardId") String boardId, @Param("listNum") Long listNum);

    @Query(value = "SELECT count(*) FROM articleEntity WHERE boardId = :boardId and category = :category", nativeQuery = true)
    int findAllBoardListCount(@Param("boardId") String boardId, @Param("category") String category);

    @Modifying
    @Query(value = "UPDATE articleEntity SET nickname = :nickname WHERE userId = :userId", nativeQuery = true)
    void changeNicknameInArticle(@Param("nickname") String nickname, @Param("userId") String userId);

//    @Query(value = "SELECT articleId, boardId, hide, numOfComments, title, nickname, createdAt, view FROM articleEntity WHERE boardId = :boardId and category = :category ORDER BY articleId DESC LIMIT :listNum, 10", nativeQuery = true)
//    List<ArticleList> findByBoardList(@Param("boardId") String boardId, @Param("category") String category, @Param("listNum") Long listNum);
}