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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (page == null || page<=1) page = 1L;
        page = (page-1)*10;
        List<ArticleList> target = articleService.boardList("album","all",page);

        log.info(target.toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @GetMapping("/{boardId}/{category}/{page}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable Long page, @PathVariable String boardId, @PathVariable(required = false) String category){
        if (page == null || page<=1) page = 1L;

        Map<String, Object> map = new HashMap<>();
        page = (page-1)*10;
        int maxPageNum = articleService.getMaxPageNum(boardId,category);
        List<ArticleList> target = articleService.boardList(boardId,category,page);
        map.put("maxPageNum",Math.ceil((double) maxPageNum /10.0));
        map.put("articleList",target);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/view/{articleId}")
    public ResponseEntity<ArticleEntity> view(@PathVariable Long articleId){
        ArticleEntity target = null;
        try {
            target = articleService.show(articleId);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        target.setView(target.getView()+1L);
        log.info(target.getView().toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @PostMapping("/write")
    public ResponseEntity<ArticleEntity> write(@RequestBody ArticleDto dto, HttpServletRequest request){
        String userId;
        try {
            String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
        } catch(Exception e){
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
