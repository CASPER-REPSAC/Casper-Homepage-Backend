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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/api/article")
public class ArticleApiController {

    String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";

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
    public ResponseEntity<Map<String, Object>> list(@PathVariable Long page, @PathVariable String boardId, @PathVariable(required = false) String category, HttpServletRequest request){

        //권한 확인
        String userId = getUserId(request);
        if(!authCheck(boardId, userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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
    public ResponseEntity<ArticleEntity> view(@PathVariable Long articleId, HttpServletRequest request){

        //권한 확인
        String boardId = articleService.getBoardId(articleId);
        String userId = getUserId(request);
        if(!authCheck(boardId, userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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
        String userId = getUserId(request);
        if(!authCheck(dto.getBoardId(),userId)||userId.equals("guest")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else if(!(dto.getBoardId().equals("notice_board")&&userService.getAuth(userId).equals("admin"))) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserEntity userEntity = userService.show(userId);

        dto.setUserId(userEntity.getId());
        dto.setNickname(userEntity.getNickname());
        dto.setView(0L);
        dto.setNumOfComments(0L);
        Date date = new Date(System.currentTimeMillis()+3600*9*1000);
        dto.setCreatedAt(date);
        dto.setModifiedAt(date);

        ArticleEntity article = dto.toEntity();
        log.info(article.toString());
        ArticleEntity created = articleService.save(article);

        return (created != null)?
            ResponseEntity.status(HttpStatus.OK).body(created):
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }



    @DeleteMapping("delete/{articleId}")
    public ResponseEntity<ArticleEntity> delete(@PathVariable Long articleId, HttpServletRequest request){

        String userId = getUserId(request);
        if(!writerCheck(articleId,userId)) ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();;

        ArticleEntity deleted = articleService.delete(articleId);
        return (deleted != null) ?
                ResponseEntity.status(HttpStatus.OK).build():
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PatchMapping("/update/{articleId}")
    public ResponseEntity<ArticleEntity> update(@PathVariable Long articleId, @RequestBody ArticleDto dto, HttpServletRequest request){

        String userId = getUserId(request);
        if(!writerCheck(articleId,userId)) ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();;

        ArticleEntity updated = articleService.update(articleId,dto);
        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private boolean authCheck(String boardId, String userId) {
        String userAuth;
        if(userId.equals("guest")) userAuth = "guest";
        else userAuth = userService.getAuth(userId);

        if(userAuth.equals("associate") && (boardId.equals("associate_member_board")||boardId.equals("freedom_board")||boardId.equals("notice_board"))) return true;
        else if(userAuth.equals("guest") && boardId.equals("freedom_board")||boardId.equals("notice_board")) return true;
        else return userAuth.equals("full") || userAuth.equals("graduate") || userAuth.equals("admin");
    }

    private boolean writerCheck(Long articleId, String userId) {
        String creater = articleService.getCreater(articleId);
        String userAuth = userService.getAuth(userId);
        return userId.equals(creater) || userAuth.equals("admin");
    }

    private String getUserId(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            return JwtTokenUtil.getLoginId(accessToken, secretKey);
        } catch(Exception e){
            return "guest";
        }
    }
}
