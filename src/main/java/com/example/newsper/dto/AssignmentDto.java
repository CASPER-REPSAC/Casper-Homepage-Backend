package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class AssignmentDto {
    @Schema(description = "과제 ID")
    private Long assignmentId;

    @Schema(description = "과제명")
    private String title;

    @Schema(description = "과목명")
    private String category;

    @Schema(description = "과제 설명")
    private String description;

    @Schema(description = "마감일")
    private Date deadline;

    @Schema(description = "작성자 ID")
    private String userId;

    @Schema(description = "작성자 이름")
    private String name;
}
