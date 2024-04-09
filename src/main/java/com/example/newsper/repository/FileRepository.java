package com.example.newsper.repository;

import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, String> {
    @Modifying
    @Query(value = "UPDATE fileEntity SET articleId = :articleId WHERE articleId = :requestId", nativeQuery = true)
    int update(@Param("requestId") Long requestId, @Param("articleId") Long articleId);

    @Query(value = "SELECT filePath FROM fileEntity WHERE articleId = :articleId", nativeQuery = true)
    List<String> getFiles(@Param("articleId") Long articleId);

}
