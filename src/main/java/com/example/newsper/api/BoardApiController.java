package com.example.newsper.api;

import com.example.newsper.annotation.AdminOnly;
import com.example.newsper.dto.BoardDto;
import com.example.newsper.entity.BoardEntity;
import com.example.newsper.entity.BoardNameKey;
import com.example.newsper.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Board", description = "게시글 소분류 API")
@RestController
@Slf4j
@RequestMapping("/api/board")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class BoardApiController {

    @Autowired
    private BoardService boardService;

    @GetMapping("/category")
    @PermitAll
    @Operation(summary = "게시글 소분류 조회", description = "게시글 분류를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> category(
            @Parameter(description = "notice_board, freedom_board, full_board, associate_board, graduate_board")
            @RequestParam String board
    ) {
        List<String> target = boardService.findCategory(board);
        Map<String, Object> map = new HashMap<>();
        map.put("categories", target);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/add")
    @AdminOnly
    @Operation(summary = "게시글 소분류 추가", description = "게시글 소분류를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> save(
            @Parameter(description = "boardName:String, subBoardName:String")
            @RequestBody BoardDto dto
    ) {
        BoardEntity target = boardService.save(dto.toEntity());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @DeleteMapping("/delete/{boardName}/{subBoardName}")
    @AdminOnly
    @Operation(summary = "게시글 소분류 삭제", description = "게시글 소분류를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "실패")
    public ResponseEntity<?> delete(
            @Parameter(description = "boardName:String")
            @PathVariable String boardName,
            @Parameter(description = "subBoardName:String")
            @PathVariable String subBoardName
    ) {
        BoardEntity deleted = boardService.delete(boardService.find(new BoardNameKey(boardName, subBoardName)));
        return (deleted != null) ?
                ResponseEntity.status(HttpStatus.OK).build() :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PatchMapping("/patch/{boardName}/{subBoardName}")
    @AdminOnly
    @Operation(summary = "게시글 소분류 수정", description = "게시글 소분류를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> update(
            @Parameter(description = "타겟 boardName:String")
            @PathVariable String boardName,
            @Parameter(description = "타겟 subBoardName:String")
            @PathVariable String subBoardName,
            @Parameter(description = "수정할 내용 boardName:String, subBoardName:String")
            @RequestBody BoardDto dto
    ) {
        boardService.update(boardService.find(new BoardNameKey(boardName, subBoardName)), dto);
        return ResponseEntity.status(HttpStatus.OK).body(boardService.find(new BoardNameKey(dto.getBoardName(), dto.getSubBoardName())));
    }
}
