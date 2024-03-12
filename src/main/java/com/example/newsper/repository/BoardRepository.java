package com.example.newsper.repository;

import com.example.newsper.entity.ArticleList;
import com.example.newsper.entity.BoardEntity;
import com.example.newsper.entity.BoardNameKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, BoardNameKey> {
    @Modifying
    @Query(value = "UPDATE boardEntity SET boardName = :boardName1, subBoardName = :subBoardName1 WHERE boardName = :boardName0 and subBoardName = :subBoardName0", nativeQuery = true)
    void update(@Param("boardName0") String boardName0, @Param("subBoardName0") String subBoardName0, @Param("boardName1") String boardName1, @Param("subBoardName1") String subBoardName1);
}