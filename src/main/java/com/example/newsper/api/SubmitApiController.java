package com.example.newsper.api;

import com.example.newsper.annotations.AssociateOnly;
import com.example.newsper.annotations.MustAuthorized;
import com.example.newsper.constant.ErrorCode;
import com.example.newsper.constant.UserRole;
import com.example.newsper.dto.CreateSubmitDto;
import com.example.newsper.entity.AssignmentEntity;
import com.example.newsper.entity.FileEntity;
import com.example.newsper.entity.SubmitEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.SubmitRepository;
import com.example.newsper.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Submit", description = "과제 제출 API")
@RestController
@Slf4j
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
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
    @MustAuthorized
    @Operation(summary = "과제 제출 조회", description = "제출한 과제를 조회합니다.")
    public ResponseEntity<?> view(
            @Parameter(description = "과제 제출 ID")
            @PathVariable Long submitId,
            HttpServletRequest request
    ) {

        SubmitEntity submitEntity = submitService.findById(submitId);
        if (submitEntity == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        if (user.getRole() == UserRole.ASSOCIATE && !submitEntity.getUserId().equals(userId))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_VIEW_MEMBER_ONLY));

        List<Map<String, String>> files = fileService.getFileNames(submitId, "submit");

        HashMap<String, Object> map = new HashMap<>();
        map.put("submit", submitEntity);
        map.put("files", files);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/submit")
    @AssociateOnly
    @Operation(summary = "과제 제출", description = "과제를 제출합니다. 준회원만 가능합니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    public ResponseEntity<?> create(
            @Parameter(description = "과제 ID")
            @PathVariable Long assignmentId,
            @RequestBody CreateSubmitDto dto,
            HttpServletRequest request
    ) {

        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        if (submitService.hasSubmitted(assignmentId, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_SUBMIT_ONE_ONLY));
        }
        if (assignmentService.findById(assignmentId).getDeadline().getTime() < new Date().getTime()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_CLOSED));
        }
        if (dto.getUrls() != null && dto.getUrls().size() > 5) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorCodeService.setErrorCodeBody(ErrorCode.FILE_COUNT_EXCEEDED_AGAIN));
        }

        SubmitEntity created = dto.toEntity(user, assignmentId);
        submitRepository.save(created);

        if (dto.getUrls() != null) {
            for (String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                fileEntity.setConnectId(String.valueOf(created.getSubmitId()));
                fileService.modify(fileEntity);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, String>> files = fileService.getFileNames(created.getSubmitId(), "submit");
        map.put("submit", created);
        map.put("files", files);

        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @PatchMapping("/edit/{submitId}")
    @AssociateOnly
    @Operation(summary = "과제 제출 수정", description = "제출된 과제를 수정합니다. 제출자만 가능합니다.")
    public ResponseEntity<?> update(
            @Parameter(description = "과제 제출 ID")
            @PathVariable Long submitId,
            @PathVariable Long assignmentId,
            @RequestBody CreateSubmitDto dto,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        SubmitEntity submitEntity = submitService.findById(submitId);

        if (!submitEntity.getUserId().equals(userId))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_EDIT_SELF_ONLY_AGAIN));
        if (assignmentService.findById(assignmentId).getDeadline().getTime() < new Date().getTime())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_CLOSED));
        if (dto.getUrls() != null && dto.getUrls().size() > 5)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.FILE_COUNT_EXCEEDED_AGAIN));

        SubmitEntity updated = submitService.update(submitEntity, dto);

        if (dto.getUrls() != null) {
            for (String url : dto.getUrls()) {
                FileEntity fileEntity = fileService.findById(url);
                if (fileEntity.getConnectId() == null) {
                    fileEntity.setConnectId(String.valueOf(updated.getSubmitId()));
                    fileService.modify(fileEntity);
                }
            }
        }

        return (updated != null) ?
                ResponseEntity.status(HttpStatus.OK).body(updated) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/delete/{submitId}")
    @MustAuthorized
    @Operation(summary = "제출된 과제 삭제", description = "제출된 과제를 삭제합니다. 관리자, 출제자, 제출자만 가능합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<?> delete(
            @Parameter(description = "제출 ID")
            @PathVariable Long submitId,
            @PathVariable Long assignmentId,
            HttpServletRequest request
    ) {
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        SubmitEntity submitEntity = submitService.findById(submitId);
        AssignmentEntity assignmentEntity = assignmentService.findById(assignmentId);

        if (!(user.getRole() == UserRole.ADMIN || assignmentEntity.getUserId().equals(userId) || submitEntity.getUserId().equals(userId)))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_DELETE_LIMITED));
        if (assignmentService.findById(assignmentId).getDeadline().getTime() < new Date().getTime())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.ASSIGNMENT_CLOSED));

        submitService.delete(submitEntity);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
