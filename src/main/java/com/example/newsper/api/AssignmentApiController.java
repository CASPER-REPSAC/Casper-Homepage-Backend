package com.example.newsper.api;

import com.example.newsper.annotation.MemberOnly;
import com.example.newsper.annotation.Authorized;
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
    @MemberOnly
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
                if(fileEntity == null) continue;
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
    @MemberOnly
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_EDIT_NO_PERMISSION));
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
    @MemberOnly
    @Operation(summary = "과제 삭제", description = "과제를 삭제합나다. 액세스 토큰 필요.")
    public ResponseEntity<?> delete(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);

        if (!assignmentEntity.getUserId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_EDIT_NO_PERMISSION));
        }

        submitService.deleteByAssignment(assignmentId);
        assignmentService.delete(assignmentEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/list/{page}")
    @Authorized
    @Operation(summary = "과제 목록 조회", description = "과제 목록을 조회합니다.")
    public ResponseEntity<?> list(
            @Parameter(description = "게시판 페이지")
            @PathVariable Long page,
            @Parameter(description = "페이지당 항목 수")
            @RequestParam(value = "limit", defaultValue = "6") Long limit,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        if (page == null || page <= 1) page = 1L;
        if (limit == null || limit <= 0) limit = 6L;

        Map<String, Object> map = new HashMap<>();
        int assignmentCount = assignmentService.getAssignmentCount();
        List<AssignmentDto> allAssignments = assignmentService.getAllAssignments();
        allAssignments = getProgress(allAssignments, userId);

        Date now = new Date();
        allAssignments.sort((a, b) -> {
            boolean aSubmitted = a.getProgress().equals(AssignmentStatus.SUBMITTED.getStatus()) ||
                    a.getProgress().equals(AssignmentStatus.GRADED.getStatus());
            boolean bSubmitted = b.getProgress().equals(AssignmentStatus.SUBMITTED.getStatus()) ||
                    b.getProgress().equals(AssignmentStatus.GRADED.getStatus());

            if (aSubmitted && !bSubmitted) {
                return 1;
            } else if (!aSubmitted && bSubmitted) {
                return -1;
            }
            long aTimeRemaining = a.getDeadline().getTime() - now.getTime();
            long bTimeRemaining = b.getDeadline().getTime() - now.getTime();
            return Long.compare(aTimeRemaining, bTimeRemaining);
        });

        int fromIndex = (int)((page - 1) * limit);
        int toIndex = Math.min(fromIndex + limit.intValue(), allAssignments.size());

        if (fromIndex >= allAssignments.size()) {
            map.put("items", new ArrayList<AssignmentDto>());
        } else {
            List<AssignmentDto> pagedAssignments = allAssignments.subList(fromIndex, toIndex);
            map.put("items", pagedAssignments);
        }
        map.put("currentPage", page);
        map.put("totalPages", (int)Math.ceil((double)assignmentCount / limit));
        map.put("totalItems", assignmentCount);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/detail/{assignmentId}")
    @Authorized
    @Operation(summary = "과제 상세 조회", description = "과제를 상세히 조회합니다.")
    public ResponseEntity<?> detail(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
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
    @MemberOnly
    @Operation(summary = "과제 채점", description = "과제를 채점합니다.")
    public ResponseEntity<?> grade(
            @Parameter(description = "과제 ID")
            @RequestBody List<SubmitGradeDto> dtos
    ) {
        for (SubmitGradeDto dto : dtos) {
            SubmitEntity submitEntity = submitService.findById(dto.getSubmitId());
            submitEntity.setScore(dto.getScore());
            submitService.save(submitEntity);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/feedback")
    @MemberOnly
    @Operation(summary = "과제 피드백", description = "과제에 피드백을 부여합니다.")
    public ResponseEntity<?> grade(
            @Parameter(description = "과제 ID")
            @RequestBody SubmitFeedbackDto dto
    ) {
        SubmitEntity submitEntity = submitService.findById(dto.getSubmitId());
        submitEntity.setFeedback(dto.getFeedback());
        if(dto.getScore() != null) submitEntity.setScore(dto.getScore());
        submitService.save(submitEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private List<AssignmentDto> getProgress(List<AssignmentDto> dtos, String userId) {
        for (AssignmentDto dto : dtos) {
            Optional.ofNullable(submitService.findByAssignmentIdAndUserId(dto.getAssignmentId(), userId))
                    .ifPresentOrElse(
                            submitEntity -> {
                                if (submitEntity.getScore() != null) {
                                    dto.setProgress(AssignmentStatus.GRADED.getStatus());
                                } else {
                                    dto.setProgress(AssignmentStatus.SUBMITTED.getStatus());
                                }
                            },
                            () -> dto.setProgress(AssignmentStatus.NOT_SUBMITTED.getStatus())
                    );
        }
        return dtos;
    }
}
