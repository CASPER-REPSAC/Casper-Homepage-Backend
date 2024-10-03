package com.example.newsper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@Entity(name="SubmitEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class SubmitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submitId")
    private Long submitId;

    @Column(name = "assignmentId", nullable = false)
    private Long assignmentId;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "submitDate", nullable = false)
    private Date submitDate;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "score")
    private Long score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
