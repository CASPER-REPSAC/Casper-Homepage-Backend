package com.example.newsper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column(name="id")
    private String id;


}
