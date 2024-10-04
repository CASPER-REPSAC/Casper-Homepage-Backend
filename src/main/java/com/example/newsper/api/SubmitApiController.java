package com.example.newsper.api;

import com.example.newsper.dto.CommentDto;
import com.example.newsper.dto.CreateSubmitDto;
import com.example.newsper.entity.*;
import com.example.newsper.repository.SubmitRepository;
import com.example.newsper.service.*;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
@Tag(name= "Submit", description = "과제 제출 API")
@RestController
@Slf4j
@RequestMapping("/api/assignment/{assignmentId}")
public class SubmitApiController {
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmitRepository submitRepository;

    @Autowired
    private SubmitService submitService;

    @Autowired
    private ErrorCodeService errorCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;


    @GetMapping("/submit/{submitId}")
    @Operation(summary= "과제 제출 조회", description= "제출한 과제를 조회합니다. 액세스 토큰 필요.")
    public ResponseEntity<?> view(
            @Parameter(description = "과제 제출 ID")
            @PathVariable Long submitId,
            HttpServletRequest request
    ){

        SubmitEntity submitEntity = submitService.findById(submitId);
        if(submitEntity == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if(user.getRole().equals("associate") && !submitEntity.getUserId().equals(userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-701));

        List<Object> files = fileService.getFileNames(submitId,"submit");

        HashMap<String,Object> map = new HashMap<>();
        map.put("submit",submitEntity);
        map.put("files",files);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/submit")
    @Operation(summary= "과제 제출", description= "과제를 제출합니다. 액세스 토큰 필요.")
    @ApiResponse(responseCode = "201", description = "성공")
    public ResponseEntity<?> create(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            @RequestBody CreateSubmitDto dto,
            HttpServletRequest request
    ){

        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if(!user.getRole().equals("associate")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-702));
        if(submitService.findByUserId(userId) != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-703));
        if(assignmentService.findById(assignmentId).getDeadline().getTime() < new Date().getTime()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-704));
        if(dto.getUrls() != null && dto.getUrls().size() > 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-707));

        SubmitEntity created = dto.toEntity(user,assignmentId);
        submitRepository.save(created);

        if(dto.getUrls() != null) {
            for(String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                fileEntity.setConnectId(String.valueOf(created.getSubmitId()));
                fileService.modify(fileEntity);
            }
        }

        HashMap<String,Object> map = new HashMap<>();
        List<Object> files = fileService.getFileNames(created.getSubmitId(),"submit");
        map.put("submit",created);
        map.put("files",files);

        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PatchMapping("/edit/{submitId}")
    @Operation(summary= "과제 제출 수정", description= "제출된 과제를 수정합니다.")
    public ResponseEntity<?> update(
            @Parameter(description = "과제 제출 ID")
            @PathVariable Long submitId,
            @PathVariable Long assignmentId,
            @RequestBody CreateSubmitDto dto,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        SubmitEntity submitEntity = submitService.findById(submitId);

        if(!submitEntity.getUserId().equals(userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-705));
        if(assignmentService.findById(assignmentId).getDeadline().getTime() < new Date().getTime()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-704));
        if(dto.getUrls() != null && dto.getUrls().size() > 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-707));

        SubmitEntity updated = submitService.update(submitEntity,dto);

        if(dto.getUrls() != null) {
            for(String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                if(fileEntity.getConnectId() == null) {
                    fileEntity.setConnectId(String.valueOf(updated.getSubmitId()));
                    fileService.modify(fileEntity);
                }
            }
        }

        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/delete/{submitId}")
    @Operation(summary= "제출된 과제 삭제", description= "제출된 과제를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> delete(
            @Parameter(description = "제출 ID")
            @PathVariable Long submitId,
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ){

        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        SubmitEntity submitEntity = submitService.findById(submitId);
        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);

        if(!(user.getRole().equals("admin") || assignmentEntity.getUserId().equals(userId) || submitEntity.getUserId().equals(userId))) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-706));
        if(assignmentService.findById(assignmentId).getDeadline().getTime() < new Date().getTime()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-704));

        submitService.delete(submitEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
