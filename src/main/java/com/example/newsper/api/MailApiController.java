package com.example.newsper.api;

import com.example.newsper.constant.ErrorCode;
import com.example.newsper.service.ErrorCodeService;
import com.example.newsper.service.MailService;
import com.example.newsper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
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
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private ErrorCodeService errorCodeService;

    @Operation(summary = "인증 메일 전송", description = "인증 메일을 전송합니다.")
    @PreAuthorize("!isAuthenticated()")
    @PostMapping("/send")
    public ResponseEntity<?> sendEmailPath(@RequestParam(value = "email") String email) {
        if (userService.findByEmail(email) != null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCodeService.setErrorCodeBody(ErrorCode.SIGNUP_DUPLICATE_ID));
        mailService.sendEmail(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "인증코드 확인", description = "인증 코드 유효성을 확인합니다.")
    @PreAuthorize("!isAuthenticated()")
    @PostMapping("/emailkey")
    public ResponseEntity<String> sendEmailPath(@RequestParam(value = "email") String email, @RequestParam(value = "emailKey") String code) {
        log.info("/api/mail/emailKey API start");
        log.info("email : " + email);
        log.info("emailKey : " + code);
        if (mailService.verifyEmailCode(email, code)) return ResponseEntity.status(HttpStatus.OK).build();
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
