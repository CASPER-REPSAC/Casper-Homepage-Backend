package com.example.newsper.api;

import com.example.newsper.dto.ArticleDto;
import com.example.newsper.dto.CreateArticleDto;
import com.example.newsper.dto.FileDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import com.example.newsper.entity.FileEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.service.ArticleService;
import com.example.newsper.service.FileService;
import com.example.newsper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Tag(name= "Article", description = "게시글 API")
@RestController
@Slf4j
@RequestMapping("/api/article")
public class ArticleApiController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @GetMapping("/album/{page}")
    @Operation(summary= "앨범 조회", description= "얼범을 조회합니다.")
    public ResponseEntity<List<ArticleList>> album(@Parameter(description = "게시판 페이지") @PathVariable Long page){
        if (page == null || page<=1) page = 1L;
        page = (page-1)*10;
        List<ArticleList> target = articleService.boardList("album","all",page);

        log.info(target.toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @GetMapping("/{boardId}/{category}/{page}")
    @Operation(summary= "게시글 리스트 조회", description= "총 페이지 수와 게시글 리스트를 반환합니다. 액세스 토큰 필요.")
    public ResponseEntity<?> list(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page,
            @Parameter(description = "notice_board, associate_board, freedom_board, full_board, graduation_board")
            @PathVariable String boardId,
            @Parameter(description = "소분류:String")
            @PathVariable(required = false) String category,
            HttpServletRequest request
    ){

        //권한 확인
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if(!articleService.authCheck(boardId, user)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-301));

        if (page == null || page<=1) page = 1L;
        Map<String, Object> map = new HashMap<>();
        page = (page-1)*10;
        int maxPageNum = articleService.getMaxPageNum(boardId,category);
        List<ArticleList> target = articleService.boardList(boardId,category,page);
        map.put("maxPageNum",Math.ceil((double) maxPageNum /10.0));
        map.put("articleList", target);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/view/{articleId}")
    @Operation(summary= "게시글 상세 조회", description= "게시글 내용을 반환합니다. 액세스 토큰 필요.")
    public ResponseEntity<?> view(@Parameter(description = "게시글ID") @PathVariable Long articleId, HttpServletRequest request){
        log.info("View API Logging");
        HashMap<String,Object> map = new HashMap<>();

        ArticleEntity article = articleService.findById(articleId);
        if(article == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if(!articleService.authCheck(article.getBoardId(), user) || !articleService.isHide(article,user)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-301));

        article.setView(article.getView()+1L);
        log.info(article.getView().toString());
        List<Object> files = fileService.getFileNames(articleId);

        UserEntity author = userService.findById(article.getUserId());

        map.put("article",article.addAuthorInfo(author.getProfileImgPath(),author.getIntroduce()));
        map.put("files",files);

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/write")
    @Operation(summary= "게시글 작성", description= "액세스 토큰 필요.")
    public ResponseEntity<?> write(
            @RequestBody CreateArticleDto _dto,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if(!articleService.authCheck(_dto.getBoardId(),user)||user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-302));
        if(_dto.getBoardId().equals("notice_board")&&!(userService.getAuth(userId).equals("admin"))) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-302));

        ArticleEntity created = articleService.write(_dto.toArticleDto(),user);

        if(_dto.getUrls() != null) {
            for(String url : _dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                fileEntity.setConnectId(String.valueOf(created.getArticleId()));
                fileService.modify(fileEntity);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(created);
    }

    @DeleteMapping("delete/{articleId}")
    @Operation(summary= "게시글 삭제", description= "게시글을 삭제합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> delete(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        ArticleEntity article = articleService.findById(articleId);

        if(!articleService.writerCheck(article,user)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-303));

        List<String> urls = fileService.getUrls(String.valueOf(articleId));

        for(String url : urls){
            fileService.delete(url);
        }

        articleService.delete(article);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/update/{articleId}")
    @Operation(summary= "게시글 수정", description= "게시글을 수정합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> update(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            @Parameter(description = "게시글DTO")
            @RequestBody ArticleDto dto,
            HttpServletRequest request
    ) throws IOException {
        log.info("Article update API Logging");
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        ArticleEntity article = articleService.findById(articleId);
        if(!articleService.writerCheck(article,user)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-303));

        ArticleEntity updated = articleService.update(articleId,dto);

        if(dto.getUrls() != null) {
            for(String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                if(fileEntity.getConnectId() == null) {
                    fileEntity.setConnectId(String.valueOf(updated.getArticleId()));
                    fileService.modify(fileEntity);
                }
            }
        }

        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private Map<String, Object> setErrorCodeBody(int code){
        Map<String, Object> responseBody = new HashMap<>();
        //responseBody.put("status", HttpStatus.UNAUTHORIZED.value());
        //responseBody.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        responseBody.put("code", code);

        if(code == -101) responseBody.put("message", "로그인 아이디를 찾을 수 없음");
        else if(code == -102) responseBody.put("message", "로그인 패스워드 불일치");
        else if(code == -201) responseBody.put("message", "회원 가입 파라미터 누락");
        else if(code == -202) responseBody.put("message", "회원 가입 이메일 인증 오류");
        else if(code == -203) responseBody.put("message", "회원 가입 ID 중복");
        else if(code == -301) responseBody.put("message", "게시판 접근 권한 없음");
        else if(code == -302) responseBody.put("message", "게시글 쓰기 권한 없음");
        else if(code == -303) responseBody.put("message", "게시글 수정/삭제 권한 없음");
        else if(code == -1) responseBody.put("message", "지정되지 않은 에러");

        return responseBody;
    }
}
