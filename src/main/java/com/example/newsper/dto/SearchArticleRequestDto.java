package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchArticleRequestDto {
    @Schema(description = "검색어")
    private String query;

    @Schema(description = "검색 타입(title, content)")
    private String type;

    @Schema(description = "게시판 ID")
    private String boardId;

    @Schema(description = "카테고리")
    private String category;

    @Schema(description = "페이지")
    private Integer page = 0;

    @Schema(description = "페이지 크기")
    private Integer size = 10;

    @Schema(description = "정렬 필드")
    private String sortField = "createdAt";

    @Schema(description = "정렬 방향 (asc/desc)")
    private String direction = "desc";
}
