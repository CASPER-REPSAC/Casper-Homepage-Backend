package com.example.newsper.api;

import com.example.newsper.dto.AddCommentDto;
import com.example.newsper.dto.CommentDto;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.service.CommentService;
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

import java.util.List;

@Tag(name= "Comment", description = "댓글 API")
@RestController
@Slf4j
@RequestMapping("/api/article/{articleId}")
public class CommentApiController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/comment")
    @Operation(summary= "댓글 조회", description= "특정 글의 댓글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<List<CommentDto>> comments(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId
    ){
        List<CommentDto> dtos = commentService.comments(articleId);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @PostMapping("/comment")
    @Operation(summary= "댓글 작성", description= "게시글에 댓글을 작성합니다. 액세스 토큰 필요.")
    @ApiResponse(responseCode = "201", description = "성공")
    public ResponseEntity<CommentEntity> create(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            @RequestBody AddCommentDto dto,
            HttpServletRequest request
    ){
        CommentEntity created = commentService.create(articleId,dto,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/comment/{id}")
    @Operation(summary= "댓글 수정", description= "댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<CommentEntity> update(
            @Parameter(description = "댓글ID")
            @PathVariable Long id,
            @Parameter(description = "text:String")
            @RequestBody CommentDto dto
    ){
        CommentEntity updated = commentService.update(id,dto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @DeleteMapping("/comment/{id}")
    @Operation(summary= "댓글 삭제", description= "댓글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<CommentEntity> delete(
            @Parameter(description = "댓글ID")
            @PathVariable Long id,
            @PathVariable String articleId
    ){
        commentService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
