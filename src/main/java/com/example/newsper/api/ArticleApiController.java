package com.example.newsper.api;

import com.example.newsper.dto.ArticleDto;
import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleMapping;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.repository.ArticleRepository;
import com.example.newsper.repository.UserRepository;
import com.example.newsper.service.ArticleService;
import com.example.newsper.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@Slf4j
@RequestMapping("/api/article")
public class ArticleApiController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @GetMapping("/album/{listNum}")
    public ResponseEntity<List<ArticleMapping>> album(@PathVariable Long listNum){
        List<ArticleMapping> target = articleService.boardList("album","전체",listNum);
        return (target != null)?
                ResponseEntity.status(HttpStatus.OK).body(target):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/boards/{boardId}/{category}")
    public ResponseEntity<List<ArticleMapping>> list(@RequestParam(value = "page") Long page, @PathVariable String boardId, @PathVariable(required = false) String category){
        if (category == null) category = "전체";
        if (page == null || page<=1) page = 0L;

        List<ArticleMapping> target = articleService.boardList(boardId,category,page);

        return (target != null)?
                ResponseEntity.status(HttpStatus.OK).body(target):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/boards/{boardId}/{category}/{articleId}")
    public ResponseEntity<ArticleEntity> view(@PathVariable Long articleId, @PathVariable(required = false) String boardId, @PathVariable(required = false) Long category){
        ArticleEntity target = articleService.show(articleId);

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
