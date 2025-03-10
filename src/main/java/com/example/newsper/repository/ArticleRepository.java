package com.example.newsper.repository;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {
    @Query(value = "SELECT articleId, boardId, hide, numOfComments, title, nickname, createdAt, view, file, category FROM articleEntity WHERE boardId = :boardId and category = :category ORDER BY articleId DESC LIMIT :listNum, 10", nativeQuery = true)
    List<ArticleList> findByBoardList(@Param("boardId") String boardId, @Param("category") String category, @Param("listNum") Long listNum);

    @Query(value = "SELECT articleId, boardId, hide, numOfComments, title, nickname, createdAt, view, file, category FROM articleEntity WHERE boardId = :boardId ORDER BY articleId DESC LIMIT :listNum, 10", nativeQuery = true)
    List<ArticleList> findByBoardListAll(@Param("boardId") String boardId, @Param("listNum") Long listNum);

    @Query(value = "SELECT count(*) FROM articleEntity WHERE boardId = :boardId and category = :category", nativeQuery = true)
    int findAllBoardListCount(@Param("boardId") String boardId, @Param("category") String category);

    @Query(value = "SELECT count(*) FROM articleEntity WHERE boardId = :boardId", nativeQuery = true)
    int findAllBoardListCount2(@Param("boardId") String boardId);


    @Modifying
    @Query(value = "UPDATE articleEntity SET nickname = :nickname WHERE userId = :userId", nativeQuery = true)
    void changeNicknameInArticle(@Param("nickname") String nickname, @Param("userId") String userId);

    @Query(value = "SELECT * FROM articleEntity WHERE boardId = :boardId and category = :category", nativeQuery = true)
    List<ArticleEntity> findByBoardName(@Param("boardId") String boardId, @Param("category") String category);

    // 제목으로 검색
    Page<ArticleEntity> findByTitleContaining(String title, Pageable pageable);

    // 내용으로 검색
    Page<ArticleEntity> findByContentContaining(String content, Pageable pageable);

    // 제목 또는 내용으로 검색
    Page<ArticleEntity> findByTitleContainingOrContentContaining(
            String title, String content, Pageable pageable);

    // 제목 + 특정 게시판으로 검색
    Page<ArticleEntity> findByTitleContainingAndBoardId(
            String title, String boardId, Pageable pageable);

    // 내용 + 특정 게시판으로 검색
    Page<ArticleEntity> findByContentContainingAndBoardId(
            String content, String boardId, Pageable pageable);

    // 제목/내용 + 특정 게시판으로 검색
    @Query("SELECT a FROM articleEntity a WHERE (a.title LIKE %:title% OR a.content LIKE %:content%) AND a.boardId = :boardId")
    Page<ArticleEntity> findByTitleContainingOrContentContainingAndBoardId(
            @Param("title") String title,
            @Param("content") String content,
            @Param("boardId") String boardId,
            Pageable pageable);

    // 제목 + 특정 게시판 + 특정 카테고리로 검색
    Page<ArticleEntity> findByTitleContainingAndBoardIdAndCategory(
            String title, String boardId, String category, Pageable pageable);

    // 내용 + 특정 게시판 + 특정 카테고리로 검색
    Page<ArticleEntity> findByContentContainingAndBoardIdAndCategory(
            String content, String boardId, String category, Pageable pageable);

    // 제목/내용 + 특정 게시판 + 특정 카테고리로 검색
    @Query("SELECT a FROM articleEntity a WHERE (a.title LIKE %:title% OR a.content LIKE %:content%) AND a.boardId = :boardId AND a.category = :category")
    Page<ArticleEntity> findByTitleContainingOrContentContainingAndBoardIdAndCategory(
            @Param("title") String title,
            @Param("content") String content,
            @Param("boardId") String boardId,
            @Param("category") String category,
            Pageable pageable);
    // 여러 게시판에서 제목으로 검색
    Page<ArticleEntity> findByTitleContainingAndBoardIdIn(
            String title, List<String> boardIds, Pageable pageable);

    // 여러 게시판에서 내용으로 검색
    Page<ArticleEntity> findByContentContainingAndBoardIdIn(
            String content, List<String> boardIds, Pageable pageable);

    // 여러 게시판에서 제목+내용으로 검색
    @Query("SELECT a FROM articleEntity a WHERE (a.title LIKE %:query% OR a.content LIKE %:query%) AND a.boardId IN :boardIds")
    Page<ArticleEntity> findByTitleOrContentContainingAndBoardIdIn(
            @Param("query") String query,
            @Param("boardIds") List<String> boardIds,
            Pageable pageable);

    // 여러 게시판에서 카테고리와 함께 제목으로 검색
    Page<ArticleEntity> findByTitleContainingAndBoardIdInAndCategory(
            String title, List<String> boardIds, String category, Pageable pageable);

    // 여러 게시판에서 카테고리와 함께 내용으로 검색
    Page<ArticleEntity> findByContentContainingAndBoardIdInAndCategory(
            String content, List<String> boardIds, String category, Pageable pageable);

    // 여러 게시판에서 카테고리와 함께 제목+내용으로 검색
    @Query("SELECT a FROM articleEntity a WHERE (a.title LIKE %:query% OR a.content LIKE %:query%) AND a.boardId IN :boardIds AND a.category = :category")
    Page<ArticleEntity> findByTitleOrContentContainingAndBoardIdInAndCategory(
            @Param("query") String query,
            @Param("boardIds") List<String> boardIds,
            @Param("category") String category,
            Pageable pageable);
}