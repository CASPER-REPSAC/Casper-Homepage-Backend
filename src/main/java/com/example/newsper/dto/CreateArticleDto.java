package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateArticleDto {
    @Schema(description = "보드 타입 : notice_board, freedom_board, associate_board, full_board, graduate_board")
    private String boardId;

    @Schema(description = "소분류")
    private String category;

    @Schema(description = "비밀글 여부")
    private Boolean hide;

    @Schema(description = "공지 설정 여부")
    private Boolean notice;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "내용")
    private String content;

    @Schema(description = "사진 주소")
    private String photo;

    @Schema(description = "파일 URLs")
    private List<String> urls;


    public ArticleDto toArticleDto(){
        return new ArticleDto(null,null,boardId,category,null,null,null,title,content,hide,notice,null,null,urls);
    }
}
