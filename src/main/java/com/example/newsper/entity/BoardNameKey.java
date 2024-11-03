package com.example.newsper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class BoardNameKey implements Serializable {
    @NotNull
    @Column(name = "boardName")
    private String boardName;

    @NotNull
    @Column(name = "subBoardName")
    private String subBoardName;
}
