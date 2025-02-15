package com.example.newsper.repository;

import com.example.newsper.entity.SubmitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmitRepository extends JpaRepository<SubmitEntity, Long> {

    @Query(value = "SELECT * FROM submitEntity WHERE assignmentId = :assignmentId", nativeQuery = true)
    List<SubmitEntity> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Modifying
    @Query(value = "UPDATE submitEntity SET nickname = :nickname WHERE id = :id", nativeQuery = true)
    void changeNicknameInSubmit(@Param("nickname") String nickname, @Param("id") String id);

    @Query(value = "SELECT * FROM submitEntity WHERE userId = :userId", nativeQuery = true)
    SubmitEntity findByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM submitEntity WHERE assignmentId = :assignmentId AND userId = :userId", nativeQuery = true)
    SubmitEntity findByAssignmentIdAndUserId(@Param("assignmentId") Long assignmentId, @Param("userId") String userId);
}
