package com.example.newsper.api;

import com.example.newsper.constant.ErrorCode;
import com.example.newsper.dto.FileDto;
import com.example.newsper.service.ErrorCodeService;
import com.example.newsper.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Autowired
    private ErrorCodeService errorCodeService;

    @PostMapping("/upload")
    @Operation(summary= "파일 업로드", description= "파일을 업로드 합니다.")
    public ResponseEntity<?> write(
            @RequestPart(value = "files") List<MultipartFile> files,
            @Parameter(description ="article, profile, assignment, submit") @RequestParam String type
    ) throws IOException {
        List<Map<String, Object>> ret = new ArrayList<>();

        if(files != null) {
            for (MultipartFile file : files) {

                log.info("파일 이름 : " + file.getOriginalFilename());
                log.info("파일 크기 : " + file.getSize());

                if (file.getSize() > 5000000) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.FILE_SIZE_EXCEEDED));
                }

                if (file.getOriginalFilename() == null || file.getOriginalFilename().length() > 100) {
                    log.info("파일 이름이 너무 길거나 null입니다.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.FILE_NAME_INVALID));
                }

                String url = fileService.fileUpload(file,type);
                Map<String, Object> map = new HashMap<>();
                map.put("name",file.getOriginalFilename());
                map.put("url",url);
                ret.add(map);
                fileService.save(new FileDto(url, type));
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(ret);
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
