package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitFeedbackDto {
    @Schema(description = "제출 ID")
    private Long submitId;
    @Schema(description = "피드백")
    private String feedback;
}
