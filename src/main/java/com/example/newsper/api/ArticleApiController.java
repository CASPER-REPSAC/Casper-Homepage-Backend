package com.example.newsper.api;

import com.example.newsper.dto.ArticleDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.service.ArticleService;
import com.example.newsper.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

    @Autowired
    private UserService userService;

    @GetMapping("/album/{page}")
    public ResponseEntity<List<ArticleList>> album(@PathVariable Long page){
        if (page == null || page<=1) page = 0L;
        List<ArticleList> target = articleService.boardList("album","all",page);

        log.info(target.toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @GetMapping("/{boardId}/{category}/{page}")
    public ResponseEntity<List<ArticleList>> list(@PathVariable Long page, @PathVariable String boardId, @PathVariable(required = false) String category){
        if (page == null || page<=1) page = 0L;

        List<ArticleList> target = articleService.boardList(boardId,category,page);
        log.info(target.toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @GetMapping("/view/{articleId}")
    public ResponseEntity<ArticleEntity> view(@PathVariable Long articleId){
        ArticleEntity target = articleService.show(articleId);
        log.info(target.getView().toString());
        target.setView(target.getView()+1L);
        log.info(target.getView().toString());
        return (target != null)?
            ResponseEntity.status(HttpStatus.OK).body(target):
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/write")
    public ResponseEntity<ArticleEntity> write(@RequestBody ArticleDto dto, HttpServletRequest request){
        String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
        String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
        UserEntity userEntity = userService.show(userId);
        //set ArticleId

        dto.setUserId(userEntity.getId());
        dto.setNickname(userEntity.getNickname());
        dto.setView(0L);
        dto.setNumOfComments(0L);


        ArticleEntity article = dto.toEntity();
        log.info(article.toString());
        ArticleEntity created = articleService.save(article);

        return (created != null)?
            ResponseEntity.status(HttpStatus.OK).body(created):
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

    }
}
