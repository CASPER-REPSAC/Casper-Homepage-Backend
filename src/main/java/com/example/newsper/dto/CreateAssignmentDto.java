package com.example.newsper.dto;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.AssignmentEntity;
import com.example.newsper.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class CreateAssignmentDto {
    @Schema(description = "과제명")
    private String title;

    @Schema(description = "과목명")
    private String category;

    @Schema(description = "과제 설명")
    private String description;

    @Schema(description = "마감일")
    private Date deadline;

    @Schema(description = "파일 URLs")
    private List<String> urls;

    public AssignmentEntity toEntity(UserEntity user){
        return new AssignmentEntity(null,title,category,description,deadline,user.getId(),user.getName());
    }
}
