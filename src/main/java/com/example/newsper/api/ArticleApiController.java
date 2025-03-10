package com.example.newsper.api;

import com.example.newsper.annotations.MustAuthorized;
import com.example.newsper.constant.ErrorCode;
import com.example.newsper.constant.UserRole;
import com.example.newsper.dto.ArticleDto;
import com.example.newsper.dto.CreateArticleDto;
import com.example.newsper.dto.SearchArticleRequestDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import com.example.newsper.entity.FileEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Article", description = "게시글 API")
@RestController
@Slf4j
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@RequestMapping("/api/article")
public class ArticleApiController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ErrorCodeService errorCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AccountLockService accountLockService;

    @Autowired
    private SearchService searchService;

    @GetMapping("/album/{page}")
    @PermitAll
    @Operation(summary = "앨범 조회", description = "앨범을 조회합니다.")
    public ResponseEntity<?> album(@Parameter(description = "게시판 페이지") @PathVariable Long page) {
        if (page == null || page <= 1) page = 1L;
        page = (page - 1) * 10;
        List<ArticleList> target = articleService.boardList("album", "all", page);
        log.info(target.toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @GetMapping("/{boardId}/{category}/{page}")
    @PermitAll
    @Operation(summary = "게시글 리스트 조회", description = "총 페이지 수와 게시글 리스트를 반환합니다. 액세스 토큰 필요.")
    public ResponseEntity<?> list(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page,
            @Parameter(description = "notice_board, associate_board, freedom_board, full_board, graduation_board")
            @PathVariable String boardId,
            @Parameter(description = "소분류:String")
            @PathVariable(required = false) String category,
            HttpServletRequest request
    ) {

        //권한 확인
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if (!articleService.authCheck(boardId, user))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.BOARD_NO_ACCESS));

        if (page == null || page <= 1) page = 1L;
        Map<String, Object> map = new HashMap<>();
        page = (page - 1) * 10;
        int articleCount = articleService.getMaxPageNum(boardId, category);
        int maxPageNum = articleCount / 10 + (articleCount % 10 == 0 ? 0 : 1);
        List<ArticleList> target = articleService.boardList(boardId, category, page);
        log.info("{}의 총 게시물 수 : {}", category, articleCount);
        map.put("maxPageNum", maxPageNum);
        map.put("articleList", target);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/view/{articleId}")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "게시글 상세 조회", description = "게시글 내용을 반환합니다. 액세스 토큰 필요.")
    public ResponseEntity<?> view(@Parameter(description = "게시글ID") @PathVariable Long articleId, HttpServletRequest request) {
        log.info("View API Logging");
        HashMap<String, Object> map = new HashMap<>();

        ArticleEntity article = articleService.findById(articleId);
        if (article == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if (!articleService.authCheck(article.getBoardId(), user) || !articleService.isHide(article, user))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.BOARD_NO_ACCESS));

        if (!userId.equals("guest") && !accountLockService.isArticleVisited(user, article))
            article.setView(article.getView() + 1L);

        log.info(article.getView().toString());
        List<Map<String, String>> files = fileService.getFileNames(articleId, "article");

        UserEntity author = userService.findById(article.getUserId());

        if (author == null) {
            map.put("article", article.addAuthorInfo(null, null));
        } else {
            map.put("article", article.addAuthorInfo(author.getProfileImgPath(), author.getIntroduce()));
        }

        map.put("files", files);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/write")
    @MustAuthorized
    @Operation(summary = "게시글 작성", description = "게시글을 작성합니다.")
    public ResponseEntity<?> write(
            @RequestBody CreateArticleDto _dto,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if (!articleService.authCheck(_dto.getBoardId(), user) || user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.BOARD_NO_WRITE_PERMISSION));
        if (_dto.getBoardId().equals("notice_board") && user.getRole() != UserRole.ADMIN)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.BOARD_NO_WRITE_PERMISSION));

        ArticleEntity created = articleService.write(_dto.toArticleDto(), user);

        if (_dto.getUrls() != null) {
            for (String url : _dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                fileEntity.setConnectId(String.valueOf(created.getArticleId()));
                created.setFile(true);
                articleService.save(created);
                fileService.modify(fileEntity);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, String>> files = fileService.getFileNames(created.getArticleId(), "article");
        map.put("article", created);
        map.put("files", files);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @DeleteMapping("delete/{articleId}")
    @MustAuthorized
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합나다.")
    public ResponseEntity<?> delete(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        ArticleEntity article = articleService.findById(articleId);

        if (!articleService.writerCheck(article, user))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.BOARD_NO_EDIT_PERMISSION));

        commentService.deleteByArticle(articleId);
        articleService.delete(article);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/update/{articleId}")
    @MustAuthorized
    @Operation(summary = "게시글 수정", description = "게시글을 수정합나다. 작성자 또는 관리자만 가능합니다.")
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
        if (!articleService.writerCheck(article, user))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.BOARD_NO_EDIT_PERMISSION));

        ArticleEntity updated = articleService.update(articleId, dto);

        if (dto.getUrls() != null) {
            for (String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                if (fileEntity.getConnectId() == null) {
                    fileEntity.setConnectId(String.valueOf(updated.getArticleId()));
                    fileService.modify(fileEntity);
                }
            }
        }

        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @PostMapping("/search")
    @PermitAll
    @Operation(summary = "게시글 검색", description = "게시글을 검색합니다.")
    public ResponseEntity<?> searchArticles(@RequestBody SearchArticleRequestDto searchRequest, HttpServletRequest request) {
        try {
            // 현재 사용자 정보 가져오기
            String userId = userService.getUserId(request);
            UserEntity user = userService.findById(userId);

            Sort sort = Sort.by(
                    searchRequest.getDirection().equalsIgnoreCase("asc") ?
                            Sort.Direction.ASC : Sort.Direction.DESC,
                    searchRequest.getSortField()
            );

            Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

            // 사용자 권한에 따른 검색 수행
            Page<ArticleEntity> results = searchService.searchArticlesWithPermissions(
                    searchRequest.getQuery(),
                    searchRequest.getBoardId(),
                    searchRequest.getCategory(),
                    searchRequest.getType(),
                    pageable,
                    user);

            Map<String, Object> response = new HashMap<>();
            response.put("items", results.getContent());
            response.put("currentPage", results.getNumber());
            response.put("totalItems", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching articles: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("검색 중 오류가 발생했습니다.");
        }
    }

}
