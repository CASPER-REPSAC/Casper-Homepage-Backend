package com.example.newsper.api;

import com.example.newsper.dto.ArticleDto;
import com.example.newsper.dto.CreateArticleDto;
import com.example.newsper.dto.FileDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
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

    @Value("${custom.secret-key}")
    String secretKey;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @GetMapping("/album/{page}")
    @Operation(summary= "앨범 조회", description= "얼범을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<List<ArticleList>> album(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page
    ){
        if (page == null || page<=1) page = 1L;
        page = (page-1)*10;
        List<ArticleList> target = articleService.boardList("album","all",page);

        log.info(target.toString());
        return ResponseEntity.status(HttpStatus.OK).body(target);
    }

    @GetMapping("/{boardId}/{category}/{page}")
    @Operation(summary= "게시글 리스트 조회", description= "총 페이지 수와 게시글 리스트를 반환합니다. 액세스 토큰 필요.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<?> list(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page,
            @Parameter(description = "공지사항:0, 정회원:1, 준회원:2, 졸업생:3, 자유게시판:4")
            @PathVariable String boardId,
            @Parameter(description = "소분류:String")
            @PathVariable(required = false) String category,
            HttpServletRequest request){

        //권한 확인
        String userId = getUserId(request);
        if(!authCheck(boardId, userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-301));

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
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<?> view(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            HttpServletRequest request
    ){
        HashMap<String,Object> map = new HashMap<>();
        //권한 확인
        String boardId = articleService.getBoardId(articleId);
        String userId = getUserId(request);
        if(!authCheck(boardId, userId) || !isHide(articleId,userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-301));

        ArticleEntity target = null;
        try {
            target = articleService.show(articleId);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        target.setView(target.getView()+1L);
        log.info(target.getView().toString());
        List<String> files = fileService.getFiles(articleId);
        map.put("article",target);
        map.put("files",files);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/write")
    @Operation(summary= "게시글 작성", description= "액세스 토큰 필요.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<?> write(
            @Parameter(description = "Content-type:application/json, 파라미터 명: createArticleDto")
            @RequestPart(value = "createArticleDto") CreateArticleDto _dto,
            @Parameter(description = "multipart/form-data, 파라미터 명: files")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) throws IOException {
        String userId = getUserId(request);
        if(!authCheck(_dto.getBoardId(),userId)||userId.equals("guest")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-302));
        if(_dto.getBoardId().equals("notice_board")&&!(userService.getAuth(userId).equals("admin"))) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-302));

        UserEntity userEntity = userService.findById(userId);
        ArticleDto dto = _dto.toArticleDto(_dto);

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

        if(files != null) {
            for (MultipartFile file : files) {
                log.info("파일 이름 : " + file.getOriginalFilename());
                log.info("파일 타입 : " + file.getContentType());
                log.info("파일 크기 : " + file.getSize());

                File checkfile = new File(file.getOriginalFilename());
                String type = null;

                try {
                    type = Files.probeContentType(checkfile.toPath());
                    log.info("MIME TYPE : " + type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (file.getSize() > 104857600) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                String uploadFolder = "/home/casper/newsper_files";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date date2 = new Date();
                String str = sdf.format(date2);
                String datePath = str.replace("-", File.separator);

                File uploadPath = new File(uploadFolder, datePath);

                if (uploadPath.exists() == false) {
                    uploadPath.mkdirs();
                }

                /* 파일 이름 */
                String uploadFileName = file.getOriginalFilename();

                /* UUID 설정 */
                String uuid = UUID.randomUUID().toString();
                uploadFileName = uuid + "_" + uploadFileName;

                /* 파일 위치, 파일 이름을 합친 File 객체 */
                File saveFile = new File(uploadPath, uploadFileName);

                file.transferTo(saveFile);

                String serverUrl = "http://build.casper.or.kr";
                String profileUrl = serverUrl + "/profile/" + datePath + "/" + uploadFileName;

                fileService.save(new FileDto(profileUrl, created.getArticleId()));
            }
        }

        return (created != null)?
            ResponseEntity.status(HttpStatus.OK).body(created):
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

//    @PostMapping("/file")
//    @Operation(summary= "파일 업로드", description= "파일 URL을 반환합니다.")
//    @ApiResponse(responseCode = "200", description = "성공")
//    @ApiResponse(responseCode = "400", description = "파라미터 오류")
////    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
//    public ResponseEntity<?> update(
//            @Parameter(description = "application/json multipart/form-data 파일 리스트")
//            @RequestParam("files") List<MultipartFile> files
//    ) throws IOException {
//        HashMap<String,Long> map = new HashMap<>();
//        Long requestId = Instant.now().toEpochMilli();
//        for (MultipartFile file : files) {
//            log.info("파일 이름 : " + file.getOriginalFilename());
//            log.info("파일 타입 : " + file.getContentType());
//            log.info("파일 크기 : " + file.getSize());
//
//            File checkfile = new File(file.getOriginalFilename());
//            String type = null;
//
//            try {
//                type = Files.probeContentType(checkfile.toPath());
//                log.info("MIME TYPE : " + type);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (file.getSize() > 104857600) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//            }
//
//            String uploadFolder = "/home/casper/newsper_files";
//
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//            Date date = new Date();
//            String str = sdf.format(date);
//            String datePath = str.replace("-", File.separator);
//
//            File uploadPath = new File(uploadFolder, datePath);
//
//            if (uploadPath.exists() == false) {
//                uploadPath.mkdirs();
//            }
//
//            /* 파일 이름 */
//            String uploadFileName = file.getOriginalFilename();
//
//            /* UUID 설정 */
//            String uuid = UUID.randomUUID().toString();
//            uploadFileName = uuid + "_" + uploadFileName;
//
//            /* 파일 위치, 파일 이름을 합친 File 객체 */
//            File saveFile = new File(uploadPath, uploadFileName);
//
//            file.transferTo(saveFile);
//
//            String serverUrl = "http://build.casper.or.kr";
//            String profileUrl = serverUrl + "/profile/" + datePath + "/" + uploadFileName;
//
//            fileService.save(new FileDto(profileUrl,requestId));
//        }
//        map.put("requestId",requestId);
//        return ResponseEntity.status(HttpStatus.OK).body(map);
//    }



    @DeleteMapping("delete/{articleId}")
    @Operation(summary= "게시글 삭제", description= "게시글을 삭제합나다. 액세스 토큰 필요.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<?> delete(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            HttpServletRequest request
    ){

        String userId = getUserId(request);
        if(!writerCheck(articleId,userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-303));

        ArticleEntity deleted = articleService.delete(articleId);
        return (deleted != null) ?
                ResponseEntity.status(HttpStatus.OK).build():
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PatchMapping("/update/{articleId}")
    @Operation(summary= "게시글 삭제", description= "게시글을 삭제합나다. 액세스 토큰 필요.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<?> update(
            @Parameter(description = "게시글ID")
            @PathVariable Long articleId,
            @Parameter(description = "게시글DTO")
            @RequestBody ArticleDto dto,
            HttpServletRequest request
    ){

        String userId = getUserId(request);
        if(!writerCheck(articleId,userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-303));

        ArticleEntity updated = articleService.update(articleId,dto);
        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private boolean authCheck(String boardId, String userId) {
        String userAuth;
        if(userId.equals("guest")) userAuth = "guest";
        else userAuth = userService.getAuth(userId);
        System.out.println("userAuth = " + userAuth);
        System.out.println("boardId = " + boardId);
        if(userAuth.equals("associate") && (boardId.equals("associate_member_board")||boardId.equals("freedom_board")||boardId.equals("notice_board"))) return true;
        else if(userAuth.equals("guest") && (boardId.equals("freedom_board")||boardId.equals("notice_board"))) return true;
        else return userAuth.equals("active") || userAuth.equals("rest") || userAuth.equals("graduate") || userAuth.equals("admin");
    }

    private boolean isHide(Long articleId, String userId) {
        String userAuth;
        if(!articleService.getHide(articleId)) return true;
        if(userId.equals("guest")) userAuth = "guest";
        else userAuth = userService.getAuth(userId);

        if(userAuth.equals("associate")) return articleService.getCreater(articleId).equals(userId);
        else return !userAuth.equals("guest");
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
