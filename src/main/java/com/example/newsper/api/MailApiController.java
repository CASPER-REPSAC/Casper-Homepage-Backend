package com.example.newsper.api;

import com.example.newsper.service.MailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@RestController
@Slf4j
@Tag(name= "Mail", description = "인증 메일 API")
@RequestMapping("/api/mail")
public class MailApiController {

    @Autowired
    private MailService mailService;

    // 인증 이메일 전송
    @PostMapping("/verify")
    public Boolean sendEmailAndCode(@RequestParam(value = "email") String email_addr, @RequestParam(value = "code") String code) {
        return mailService.verifyEmailCode(email_addr, code);
    }

    // 인증번호 일치여부 확인
    @GetMapping("/send")
    public ResponseEntity<String> sendEmailPath(@RequestParam(value = "email") String email_addr) {
        mailService.sendEmail(email_addr);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
