package com.example.newsper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name = "AssignmentEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class AssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignmentId")
    private Long assignmentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline", nullable = false)
    private Date deadline;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;
}
