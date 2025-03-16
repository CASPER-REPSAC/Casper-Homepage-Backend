package com.example.newsper.api;

import com.example.newsper.constant.ErrorCode;
import com.example.newsper.service.ErrorCodeService;
import com.example.newsper.service.MailService;
import com.example.newsper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "Mail", description = "인증 메일 API")
@RequestMapping("/api/mail")
public class MailApiController {
    @Autowired
    private ErrorCodeService errorCodeService;

    @Operation(summary = "인증 메일 전송", description = "인증 메일을 전송합니다.")
    @PreAuthorize("!isAuthenticated()")
    @PostMapping("/send")
    // revert: see code before commit 1e2fbd6
    public ResponseEntity<?> sendEmailPath(@RequestParam(value = "email") String ignoredEmail) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorCodeService.setErrorCodeBody(ErrorCode.DISABLED_FEATURE));
    }

    @Operation(summary = "인증코드 확인", description = "인증 코드 유효성을 확인합니다.")
    @PreAuthorize("!isAuthenticated()")
    @PostMapping("/emailkey")
    // revert: see code before commit 1e2fbd6
    public ResponseEntity<?> sendEmailPath(@RequestParam(value = "email") String ignoredEmail, @RequestParam(value = "emailKey") String ignoredCode) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorCodeService.setErrorCodeBody(ErrorCode.DISABLED_FEATURE));
    }
}
