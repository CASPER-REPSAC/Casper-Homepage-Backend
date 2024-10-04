package com.example.newsper.api;

import com.example.newsper.dto.*;
import com.example.newsper.entity.*;
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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name= "Assignment", description = "과제 API")
@RestController
@Slf4j
@RequestMapping("/api/assignment")
public class AssignmentApiController {
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmitService submitService;

    @Autowired
    private ErrorCodeService errorCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @PostMapping("/create")
    @Operation(summary= "과제 작성", description= "액세스 토큰 필요.")
    public ResponseEntity<?> write(
            @RequestBody CreateAssignmentDto dto,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if(user == null || user.getRole().equals("associate")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-601));
        if(dto.getUrls().size() > 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-604));

        AssignmentEntity created = dto.toEntity(user);
        assignmentService.save(created);

        if(dto.getUrls() != null) {
            for(String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                fileEntity.setConnectId(String.valueOf(created.getAssignmentId()));
                fileService.modify(fileEntity);
            }
        }

        HashMap<String,Object> map = new HashMap<>();
        List<Object> files = fileService.getFileNames(created.getAssignmentId(),"assignment");
        map.put("assignment", created);
        map.put("files", files);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PatchMapping("/edit/{assignmentId}")
    @Operation(summary= "과제 수정", description= "과제를 수정합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> update(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            @Parameter(description = "과제 DTO")
            @RequestBody CreateAssignmentDto dto,
            HttpServletRequest request
    ) throws IOException {
        String userId = userService.getUserId(request);

        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);
        if(!assignmentEntity.getUserId().equals(userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-602));
        if(dto.getUrls().size() > 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-604));

        AssignmentEntity updated = assignmentService.update(assignmentEntity,dto);

        if(dto.getUrls() != null) {
            for(String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                if(fileEntity.getConnectId() == null) {
                    fileEntity.setConnectId(String.valueOf(updated.getAssignmentId()));
                    fileService.modify(fileEntity);
                }
            }
        }

        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).build():
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("delete/{assignmentId}")
    @Operation(summary= "과제 삭제", description= "과제를 삭제합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> delete(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);

        if(!assignmentEntity.getUserId().equals(userId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-602));

        submitService.deleteByAssignment(assignmentId);
        assignmentService.delete(assignmentEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/list/{page}")
    @Operation(summary= "과제 목록 조회", description= "과제 목록을 조회합니다.")
    public ResponseEntity<?> list(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        if(userId.equals("guest")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-603));
        if (page == null || page<=1) page = 1L;
        Map<String, Object> map = new HashMap<>();
        page = (page-1)*10;
        double maxPageNum = assignmentService.getMaxPageNum();
        List<AssignmentListDto> dtos = assignmentService.assignmentList(page);

        map.put("AssignmentList", getProgress(dtos,userId));
        map.put("maxPageNum",Math.ceil(maxPageNum/10.0));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/detail/{assignmentId}")
    @Operation(summary= "과제 상세 조회", description= "과제를 상세히 조회합니다.")
    public ResponseEntity<?> detail(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        if(userId.equals("guest")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-603));
        UserEntity user = userService.findById(userId);

        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);
        Map<String, Object> map = new HashMap<>();
        map.put("assignment",assignmentEntity);

        List<Object> assignmentFiles = fileService.getFileNames(assignmentId,"assignment");
        map.put("assignmentFiles", assignmentFiles);

        if(!(user.getRole().equals("associate")||user.getRole().equals("guest"))){
            List<SubmitListDto> dtos = submitService.findByAssignmentId(assignmentId);
            for(SubmitListDto dto : dtos){
                List<Object> files = fileService.getFileNames(dto.getSubmitId(),"submit");
                dto.setUrls(files);
            }

            map.put("submit",dtos);
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/grade")
    @Operation(summary= "과제 채점", description= "과제를 채점합니다.")
    public ResponseEntity<?> grade(
            @Parameter(description = "과제 ID")
            @RequestBody List<SubmitGradeDto> dtos,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if(user.getRole().equals("associate")||user.getRole().equals("guest")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-601));
        for(SubmitGradeDto dto : dtos){
            SubmitEntity submitEntity = submitService.findById(dto.getSubmitId());
            submitEntity.setScore(dto.getScore());
            submitService.save(submitEntity);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/feedback")
    @Operation(summary= "과제 피드백", description= "과제에 피드백을 부여합니다.")
    public ResponseEntity<?> grade(
            @Parameter(description = "과제 ID")
            @RequestBody SubmitFeedbackDto dto,
            HttpServletRequest request
    ){
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if(user.getRole().equals("associate")||user.getRole().equals("guest")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(-601));

        SubmitEntity submitEntity = submitService.findById(dto.getSubmitId());
        submitEntity.setFeedback(dto.getFeedback());
        submitService.save(submitEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private List<AssignmentListDto> getProgress(List<AssignmentListDto> dtos, String userId){
        for(AssignmentListDto dto : dtos){
            SubmitEntity submitEntity = submitService.findByUserId(userId);
            Date date = new Date();
            if(date.getTime() >= dto.getDeadline().getTime()){
                dto.setProgress("마감됨");
            } else{
                if(submitEntity != null && submitEntity.getAssignmentId().equals(dto.getAssignmentId())) {
                    if(submitEntity.getScore() != null)
                        dto.setProgress("채점완료");
                    else
                        dto.setProgress("제출완료");
                } else{
                    dto.setProgress("진행중");
                }
            }
        }
        return dtos;
    }
}
