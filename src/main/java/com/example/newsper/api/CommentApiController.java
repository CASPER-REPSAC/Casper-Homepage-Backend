package com.example.newsper.api;

import com.example.newsper.dto.AddCommentDto;
import com.example.newsper.dto.CommentDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.service.ArticleService;
import com.example.newsper.service.CommentService;
import com.example.newsper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name= "Comment", description = "댓글 API")
@RestController
@Slf4j
@RequestMapping("/api/article/{articleId}")
public class CommentApiController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @GetMapping("/comment")
    @Operation(summary= "댓글 조회", description= "특정 글의 댓글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> comments(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if(!commentService.authCheck(articleId, request) || !articleService.isHide(articleService.findById(articleId),user)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-301));

        List<CommentDto> dtos = commentService.comments(articleId);
        commentService.commentCount(articleId,dtos.size());

        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @PostMapping("/comment")
    @Operation(summary= "댓글 작성", description= "게시글에 댓글을 작성합니다. 액세스 토큰 필요.")
    @ApiResponse(responseCode = "201", description = "성공")
    public ResponseEntity<?> create(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            @RequestBody AddCommentDto dto,
            HttpServletRequest request
    ){
        if(!commentService.authCheck(articleId,request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-302));
        commentService.commentCount(articleId,1);

        CommentEntity created = commentService.create(articleId,dto,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/comment/{id}")
    @Operation(summary= "댓글 수정", description= "댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> update(
            @Parameter(description = "댓글ID")
            @PathVariable Long id,
            @Parameter(description = "text:String")
            @RequestBody CommentDto dto,
            HttpServletRequest request
    ){

        if(!commentService.writerCheck(commentService.findById(id), request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-303));

        CommentEntity updated = commentService.update(id,dto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/comment/{id}")
    @Operation(summary= "댓글 삭제", description= "댓글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> delete(
            @Parameter(description = "댓글ID")
            @PathVariable Long articleId,
            @PathVariable Long id,
            HttpServletRequest request
){

        if(!commentService.writerCheck(commentService.findById(id), request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-303));
        commentService.commentCount(articleId,-1);

        commentService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
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
