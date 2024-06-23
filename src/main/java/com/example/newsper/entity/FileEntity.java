package com.example.newsper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Entity(name="fileEntity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Slf4j
public class FileEntity {
    @Id
    @Column(name="filePath")
    private String filePath;

    @NotNull
    @Column(name="type")
    private String type;

    @Column(name="connectId")
    private String connectId;

}
