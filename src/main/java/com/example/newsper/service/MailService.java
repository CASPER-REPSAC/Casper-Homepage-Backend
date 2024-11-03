package com.example.newsper.service;

import com.example.newsper.entity.UserEntity;
import com.example.newsper.redis.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final RedisUtil redisUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String configEmail;

    private String createdCode() {
        int leftLimit = 48; // number '0'
        int rightLimit = 122; // alphabet 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public MimeMessage joinMail(String mail) {
        String authCode = createdCode();

        MimeMessage message = mailSender.createMimeMessage();

        try {
            message.setFrom(configEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("캐스퍼 회원가입 이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + authCode + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body, "UTF-8", "html");

            redisUtil.setDataExpire(mail, authCode, 60 * 5L);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public void idMail(String mail, String id) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            message.setFrom(configEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("캐스퍼 ID 찾기");
            String body = "";
            body += "<h3>" + "요청하신 ID 입니다." + "</h3>";
            body += "<h1>" + id + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body, "UTF-8", "html");

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void pwMail(UserEntity user) {

        String authCode = createdCode();
        user.setPw(passwordEncoder.encode(authCode));
        userService.modify(user);
        MimeMessage message = mailSender.createMimeMessage();

        try {
            message.setFrom(configEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, user.getEmail());
            message.setSubject("캐스퍼 PW 초기화");
            String body = "";
            body += "<h3>" + "초기화된 PW 입니다." + "</h3>";
            body += "<h1>" + authCode + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body, "UTF-8", "html");

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(String toEmail) {
        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        MimeMessage emailForm = joinMail(toEmail);

        mailSender.send(emailForm);
    }

    public Boolean verifyEmailCode(String email, String code) {
        String codeFoundByEmail = redisUtil.getData(email);
        if (codeFoundByEmail == null) {
            return false;
        }
        return codeFoundByEmail.equals(code);
    }
}