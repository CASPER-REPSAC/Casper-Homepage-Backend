package com.example.newsper.api;

import com.example.newsper.dto.FileDto;
import com.example.newsper.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name= "Article", description = "게시글 API")
@RestController
@Slf4j
@RequestMapping("/api/file")
public class FileApiController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @Operation(summary= "파일 업로드", description= "파일을 업로드 합니다.")
    public ResponseEntity<?> write(
            @RequestPart(value = "files") List<MultipartFile> files,
            @Parameter(description ="article, file") @RequestParam String type
    ) throws IOException {
        Map<String, Object> map = new HashMap<>();
        List<String> urls = new ArrayList<>();

        if(files != null) {
            for (MultipartFile file : files) {

                log.info("파일 이름 : " + file.getOriginalFilename());
                log.info("파일 크기 : " + file.getSize());

                if (file.getSize() > 10000000) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-401));
                }

                String url = fileService.fileUpload(file,type);
                urls.add(url);
                fileService.save(new FileDto(url, type));
            }
        }

        map.put("urls", urls);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @DeleteMapping("/delete")
    @Operation(summary= "파일 삭제", description= "파일을 삭제합니다.")
    public ResponseEntity<?> write(
            @RequestParam String url
    ){
        fileService.delete(url);
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
        else if(code == -401) responseBody.put("message", "파일 용량 10MB 초과");
        else if(code == -1) responseBody.put("message", "지정되지 않은 에러");

        return responseBody;
    }
}
