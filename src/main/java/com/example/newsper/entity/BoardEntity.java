package com.example.newsper.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name="boardEntity")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class BoardEntity {
    @EmbeddedId
    private BoardNameKey boardNameKey;

//    @Column(name = "boardName")
//    private String boardName;
//
//    @Column(name = "subBoardName")
//    private String subBoardName;
}
