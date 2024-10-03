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

//    @Modifying
//    @Query(value = "UPDATE fileEntity SET id = :id WHERE filePath = :url", nativeQuery = true)
//    void update(@Param("connectId") String requestId, @Param("id") String id);

    @Query(value = "SELECT filePath FROM fileEntity WHERE connectId = :connectId AND type = :type", nativeQuery = true)
    List<String> getUrls(@Param("connectId") String connectId, @Param("type") String type);


//    @Transactional
//    @Modifying
//    @Query(value = "DELETE FROM fileEntity WHERE id = :id", nativeQuery = true)
//    void deletebyArticleId(@Param("id") Long id);

}
