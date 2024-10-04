package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitGradeDto {
    @Schema(description = "제출 ID")
    private Long submitId;
    @Schema(description = "점수")
    private Long score;
}
