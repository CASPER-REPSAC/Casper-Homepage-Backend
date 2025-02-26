package com.example.newsper.migration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "db_version")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbVersionEntity {

    @Id
    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "description", length = 255)
    private String description;
}
