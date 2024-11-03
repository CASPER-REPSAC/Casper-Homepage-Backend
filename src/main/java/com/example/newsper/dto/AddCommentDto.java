package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AddCommentDto {
    @Schema(description = "댓글 내용")
    private String text;

    public CommentDto toCommentDto(AddCommentDto dto) {
        return new CommentDto(null, null, dto.getText(), null, null, null, null, null);
    }
}
