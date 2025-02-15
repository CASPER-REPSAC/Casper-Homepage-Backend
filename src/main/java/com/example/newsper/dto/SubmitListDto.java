package com.example.newsper.dto;

import com.example.newsper.entity.SubmitEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitListDto {

    @Schema(description = "제출 ID")
    private Long submitId;

    @Schema(description = "제출자 이름")
    private String name;

    @Schema(description = "제출자 ID")
    private String userId;

    @Schema(description = "제출일")
    private Date submitDate;

    @Schema(description = "내용")
    private String content;

    @Schema(description = "점수")
    private Long score;

    @Schema(description = "피드백")
    private String feedback;

    @Schema(description = "파일 URLs")
    private List<Map<String, String>> files;

    public SubmitListDto(SubmitEntity submitEntity) {
        this.submitId = submitEntity.getSubmitId();
        this.name = submitEntity.getName();
        this.userId = submitEntity.getUserId();
        this.submitDate = submitEntity.getSubmitDate();
        this.content = submitEntity.getContent();
        this.score = submitEntity.getScore();
        this.feedback = submitEntity.getFeedback();
    }
}
