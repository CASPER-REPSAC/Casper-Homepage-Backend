package com.example.newsper.dto;

import com.example.newsper.entity.SubmitEntity;
import com.example.newsper.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Date;
import java.util.List;
@Getter
public class CreateSubmitDto {

    @Schema(description = "제출 내용")
    private String content;

    @Schema(description = "파일 URLs")
    private List<String> urls;

    public SubmitEntity toEntity(UserEntity user, Long assignmentId){
        return new SubmitEntity(null, assignmentId, user.getId(), user.getName(), new Date(), content, null, null);
    }
}
