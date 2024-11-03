package com.example.newsper.repository;

import com.example.newsper.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity,Long> {
    @Modifying
    @Query(value = "UPDATE assignmentEntity SET nickname = :nickname WHERE id = :id", nativeQuery = true)
    void changeNicknameInAssignment(@Param("nickname") String nickname, @Param("id") String id);

    @Query(value = "SELECT count(*) FROM assignmentEntity", nativeQuery = true)
    int assignmentCount();

    @Query(value = "SELECT assignmentId, title, category, deadline, userId, name, '마감됨' AS progress FROM assignmentEntity ORDER BY assignmentId DESC LIMIT :listNum, 10", nativeQuery = true)
    List<Object[]> AssignmentList(@Param("listNum") Long listNum);
}
