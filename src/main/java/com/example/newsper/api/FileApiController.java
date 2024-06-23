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

import java.io.IOException;
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
}
