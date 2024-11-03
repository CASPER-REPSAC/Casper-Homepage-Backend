package com.example.newsper.dto;

import com.example.newsper.entity.BoardEntity;
import com.example.newsper.entity.BoardNameKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@AllArgsConstructor
@Getter
@Setter
@ToString
public class BoardDto {
    private String boardName;
    private String subBoardName;

    public BoardEntity toEntity() {
        return new BoardEntity(new BoardNameKey(boardName, subBoardName));
    }

    public BoardNameKey toBoardNameKey() {
        return new BoardNameKey(this.boardName, this.subBoardName);
    }
}
