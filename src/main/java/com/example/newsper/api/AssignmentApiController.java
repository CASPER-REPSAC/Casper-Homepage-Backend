package com.example.newsper.api;

import com.example.newsper.constant.AssignmentStatus;
import com.example.newsper.constant.ErrorCode;
import com.example.newsper.constant.UserRole;
import com.example.newsper.dto.*;
import com.example.newsper.entity.AssignmentEntity;
import com.example.newsper.entity.FileEntity;
import com.example.newsper.entity.SubmitEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Tag(name = "Assignment", description = "과제 API")
@RestController
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
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
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 작성", description = "액세스 토큰 필요.")
    public ResponseEntity<?> write(
            @RequestBody CreateAssignmentDto dto,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if (user == null || user.getRole() == UserRole.ASSOCIATE)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_CREATION_MEMBER_ONLY));
        if (dto.getUrls() != null && dto.getUrls().size() > 5)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCodeService.setErrorCodeBody(ErrorCode.FILE_COUNT_EXCEEDED));

        AssignmentEntity created = dto.toEntity(user);
        assignmentService.save(created);

        if (dto.getUrls() != null) {
            for (String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                fileEntity.setConnectId(String.valueOf(created.getAssignmentId()));
                fileService.modify(fileEntity);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, String>> files = fileService.getFileNames(created.getAssignmentId(), "assignment");
        map.put("assignment", created);
        map.put("files", files);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PatchMapping("/edit/{assignmentId}")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 수정", description = "과제를 수정합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> update(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            @Parameter(description = "과제 DTO")
            @RequestBody CreateAssignmentDto dto,
            HttpServletRequest request
    ) throws IOException {
        String userId = userService.getUserId(request);

        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);
        if (!assignmentEntity.getUserId().equals(userId))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_EDIT_SELF_ONLY));
        if (dto.getUrls() != null && dto.getUrls().size() > 5)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.FILE_COUNT_EXCEEDED));

        AssignmentEntity updated = assignmentService.update(assignmentEntity, dto);

        if (dto.getUrls() != null) {
            for (String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                if (fileEntity.getConnectId() == null) {
                    fileEntity.setConnectId(String.valueOf(updated.getAssignmentId()));
                    fileService.modify(fileEntity);
                }
            }
        }

        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).build() :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/delete/{assignmentId}")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 삭제", description = "과제를 삭제합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> delete(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);

        if (!assignmentEntity.getUserId().equals(userId))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_EDIT_SELF_ONLY));

        submitService.deleteByAssignment(assignmentId);
        assignmentService.delete(assignmentEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/list/{page}")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 목록 조회", description = "과제 목록을 조회합니다.")
    public ResponseEntity<?> list(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        if (userId.equals("guest"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.LOGIN_REQUIRED));
        if (page == null || page <= 1) page = 1L;
        Map<String, Object> map = new HashMap<>();
        page = (page - 1) * 10;
        double maxPageNum = assignmentService.getMaxPageNum();
        List<AssignmentListDto> dtos = assignmentService.assignmentList(page);

        map.put("assignments", getProgress(dtos, userId));
        map.put("maxPage", Math.ceil(maxPageNum / 10.0));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/detail/{assignmentId}")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 상세 조회", description = "과제를 상세히 조회합니다.")
    public ResponseEntity<?> detail(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        if (userId.equals("guest"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.LOGIN_REQUIRED));
        UserEntity user = userService.findById(userId);

        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);
        Map<String, Object> map = new HashMap<>();
        map.put("assignment", assignmentEntity);

        List<Map<String, String>> assignmentFiles = fileService.getFileNames(assignmentId, "assignment");
        map.put("files", assignmentFiles);

        if (user.getRole() != UserRole.ASSOCIATE) {
            List<SubmitListDto> dtos = submitService.findByAssignmentId(assignmentId);
            for (SubmitListDto dto : dtos) {
                List<Map<String, String>> files = fileService.getFileNames(dto.getSubmitId(), "submit");
                dto.setFeedback(null);
                dto.setContent(null);
                dto.setFiles(files);
            }
            map.put("submits", dtos);
        }
        else {
            SubmitEntity submitEntity = submitService.findByAssignmentIdAndUserId(assignmentId, userId);
            List<SubmitListDto> dtos = new ArrayList<>();
            if (submitEntity != null) {
                List<Map<String, String>> files = fileService.getFileNames(submitEntity.getSubmitId(), "submit");
                SubmitListDto submitListDto = new SubmitListDto(submitEntity);
                submitListDto.setFeedback(null);
                submitListDto.setContent(null);
                submitListDto.setFiles(files);
                dtos.add(submitListDto);
            }
            map.put("submits", dtos);
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/grade")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 채점", description = "과제를 채점합니다.")
    public ResponseEntity<?> grade(
            @Parameter(description = "과제 ID")
            @RequestBody List<SubmitGradeDto> dtos,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if (user.getRole() == UserRole.ASSOCIATE || user.getRole() == UserRole.GUEST)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_CREATION_MEMBER_ONLY));
        for (SubmitGradeDto dto : dtos) {
            SubmitEntity submitEntity = submitService.findById(dto.getSubmitId());
            submitEntity.setScore(dto.getScore());
            submitService.save(submitEntity);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/feedback")
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "과제 피드백", description = "과제에 피드백을 부여합니다.")
    public ResponseEntity<?> grade(
            @Parameter(description = "과제 ID")
            @RequestBody SubmitFeedbackDto dto,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if (user.getRole() == UserRole.ASSOCIATE || user.getRole() == UserRole.GUEST)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_CREATION_MEMBER_ONLY));

        SubmitEntity submitEntity = submitService.findById(dto.getSubmitId());
        submitEntity.setFeedback(dto.getFeedback());
        if(dto.getScore() != null) submitEntity.setScore(dto.getScore());
        submitService.save(submitEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private List<AssignmentListDto> getProgress(List<AssignmentListDto> dtos, String userId) {
        for (AssignmentListDto dto : dtos) {
            SubmitEntity submitEntity = submitService.findByAssignmentIdAndUserId(dto.getAssignmentId(), userId);
            if (submitEntity != null) {
                if (submitEntity.getScore() != null) {
                    dto.setProgress(AssignmentStatus.GRADED.getStatus());
                }
                else {
                    dto.setProgress(AssignmentStatus.SUBMITTED.getStatus());
                }
            } else { dto.setProgress(AssignmentStatus.NOT_SUBMITTED.getStatus()); }
        }
        return dtos;
    }
}
