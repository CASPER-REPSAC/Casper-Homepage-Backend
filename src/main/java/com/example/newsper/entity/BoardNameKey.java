package com.example.newsper.entity;

import com.example.newsper.dto.BoardDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
    @Column(name = "boardName")
    private String boardName;

    @Column(name = "subBoardName")
    private String subBoardName;
}
