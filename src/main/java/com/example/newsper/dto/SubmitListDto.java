package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class SubmitListDto {

    @Schema(description = "제출 ID")
    private Long submitId;

    @Schema(description = "제출자 이름")
    private String name;

    @Schema(description = "제출일")
    private Date submitDate;

    @Schema(description = "점수")
    private Long score;

    @Schema(description = "파일 URLs")
    private List<Object> urls;
}
