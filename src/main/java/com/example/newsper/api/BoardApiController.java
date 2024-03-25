package com.example.newsper.api;

import com.example.newsper.dto.BoardDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.BoardEntity;
import com.example.newsper.entity.BoardNameKey;
import com.example.newsper.service.BoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/board/boards")
public class BoardApiController {

    @Autowired
    private BoardService boardService;

    @GetMapping("/")
    public ResponseEntity<?> findAll(){
        List<BoardEntity> dtos = boardService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody BoardDto dto){
        BoardEntity target = boardService.save(dto.toEntity());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @DeleteMapping("/{boardName}/{subBoardName}")
    public ResponseEntity<?> delete(@PathVariable String boardName, @PathVariable String subBoardName){
        BoardEntity deleted = boardService.delete(boardService.find(new BoardNameKey(boardName, subBoardName)));
        return (deleted != null) ?
                ResponseEntity.status(HttpStatus.OK).build():
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PatchMapping("/{boardName}/{subBoardName}")
    public ResponseEntity<?> update(@PathVariable String boardName, @PathVariable String subBoardName, @RequestBody BoardDto dto){
        boardService.update(boardService.find(new BoardNameKey(boardName, subBoardName)), dto);
        return ResponseEntity.status(HttpStatus.OK).body(boardService.find(new BoardNameKey(dto.getBoardName(), dto.getSubBoardName())));
    }
}
