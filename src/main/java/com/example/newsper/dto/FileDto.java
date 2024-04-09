package com.example.newsper.dto;

import com.example.newsper.entity.FileEntity;
import com.example.newsper.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class FileDto {
    private String filePath;
    private Long articleId;

    public FileEntity toEntity() {
        return new FileEntity(filePath,articleId);
    }
}
