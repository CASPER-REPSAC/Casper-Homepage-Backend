package com.example.newsper.repository;

import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, String> {
    @Query(value = "UPDATE fileEntity SET articleId = :articleId WHERE articleId = :requestId", nativeQuery = true)
    void update(@Param("requestId") Long requestId, @Param("articleId") Long articleId);

}
