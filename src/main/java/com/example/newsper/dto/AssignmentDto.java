package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class AssignmentDto {
    @Schema(description = "과제 ID")
    private Long assignmentId;

    @Schema(description = "과제명")
    private String title;

    @Schema(description = "과목명")
    private String category;

    @Schema(description = "마감일")
    private Date deadline;

    @Schema(description = "작성자 ID")
    private String userId;

    @Schema(description = "작성자 이름")
    private String name;

    @Schema(description = "진행 상태")
    private String progress;
}
