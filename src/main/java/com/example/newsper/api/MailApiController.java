package com.example.newsper.api;

import com.example.newsper.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary= "인증 메일 전송", description= "인증 메일을 전송합니다.")
    @PostMapping("/send")
    public ResponseEntity<String> sendEmailPath(@RequestParam(value = "email") String email_addr) {
        mailService.sendEmail(email_addr);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
