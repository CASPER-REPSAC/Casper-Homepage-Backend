package com.example.newsper.api;

import com.example.newsper.dto.CommentDto;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/article/{articleId}")
public class CommentApiController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/comment")
    public ResponseEntity<List<CommentDto>> comments(@PathVariable Long articleId){
        List<CommentDto> dtos = commentService.comments(articleId);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @PostMapping("/comment")
    public ResponseEntity<CommentEntity> create(@PathVariable Long articleId, @RequestBody CommentDto dto, HttpServletRequest request){
        CommentEntity created = commentService.create(articleId,dto,request);
        return ResponseEntity.status(HttpStatus.OK).body(created);
    }

    @PatchMapping("/comment/{id}")
    public ResponseEntity<CommentEntity> update(@PathVariable Long id, @RequestBody CommentDto dto){
        CommentEntity updated = commentService.update(id,dto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/comment/{id}")
    public ResponseEntity<CommentEntity> delete(@PathVariable Long id, @PathVariable String articleId){
        commentService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
