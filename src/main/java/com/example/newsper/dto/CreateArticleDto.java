package com.example.newsper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CreateArticleDto {
    @Schema(description = "보드 타입 : notice_board, free_board, associate_member_board, full_member_board, graduate_member_board")
    private String boardId;

    @Schema(description = "소분류")
    private String category;

    @Schema(description = "파일 유무")
    private Boolean file;

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

    @Schema(description = "파일 업로드 시 임시 articleId")
    private Long requestId;

    public ArticleDto toArticleDto(CreateArticleDto dto){
        return new ArticleDto(null,null,dto.getBoardId(),dto.getCategory(),null,null,null,dto.getTitle(),dto.getContent(),dto.getHide(),dto.getNotice(),null,dto.getFile(),dto.getPhoto(),null,dto.getRequestId());
    }
}
