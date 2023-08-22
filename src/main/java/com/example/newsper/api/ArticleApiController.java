package com.example.newsper.api;

import com.example.newsper.dto.ArticleDto;
import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleMapping;
import com.example.newsper.service.ArticleService;
import com.example.newsper.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/article")
public class ArticleApiController {

    @Autowired
    private ArticleService articleService;

    @PostMapping("/album")
    public ResponseEntity<List<ArticleEntity>> album(){
        List<ArticleEntity> target = articleService.album();
        return (target != null)?
                ResponseEntity.status(HttpStatus.OK).body(target):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/boards/{boardId}/{category}/{listNum}")
    public ResponseEntity<List<ArticleMapping>> list(@PathVariable String boardId, @PathVariable(required = false) Long category, @PathVariable(required = false) Long listNum){
        if (category == null) category = 0L;
        if (listNum == null || listNum<=1) listNum = 0L;

        List<ArticleMapping> target = articleService.boardList(boardId,category,listNum);

        return (target != null)?
                ResponseEntity.status(HttpStatus.OK).body(target):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @GetMapping("/list/{boardId}/{articleId}")
    public ResponseEntity<ArticleEntity> show(@PathVariable Long articleId, @PathVariable String boardId){
        ArticleEntity target = articleService.show(articleId);

        return (target != null)?
            ResponseEntity.status(HttpStatus.OK).body(target):
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/write")
    public ResponseEntity<ArticleEntity> write(@RequestBody ArticleDto dto){
        ArticleEntity article = dto.toEntity();
        ArticleEntity created = articleService.save(article);

        return (created != null)?
            ResponseEntity.status(HttpStatus.OK).body(created):
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

    }
}
