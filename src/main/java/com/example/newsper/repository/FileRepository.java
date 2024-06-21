package com.example.newsper.repository;

import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.FileEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, String> {
    @Modifying
    @Query(value = "UPDATE fileEntity SET id = :id WHERE id = :requestId", nativeQuery = true)
    int update(@Param("requestId") Long requestId, @Param("id") String id);

    @Query(value = "SELECT filePath FROM fileEntity WHERE id = :id", nativeQuery = true)
    List<String> getFiles(@Param("id") String id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM fileEntity WHERE id = :id", nativeQuery = true)
    void deletebyArticleId(@Param("id") Long id);

}
