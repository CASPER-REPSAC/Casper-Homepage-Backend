package com.example.newsper.api;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.repository.UserRepository;
import com.example.newsper.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @PostMapping("/join")
    public ResponseEntity<UserEntity> newUser(@RequestBody UserDto dto, BindingResult bindingResult, @RequestPart(value = "profile",required = false) MultipartFile imgFile){
        try {
            log.info(dto.toString());

            UserEntity created;
            if (!(imgFile == null)){
                created = userService.newUser(dto,imgFile);
            }
            else{
                created = userService.newUser(dto);
            }
            return (created != null) ?
                    ResponseEntity.status(HttpStatus.OK).body(created):
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }catch(DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<UserEntity> userWithdrawal(@RequestBody UserDto dto){
        UserEntity target = userService.show(dto.getId());
        if (target.getPw().equals(dto.getPw())){
            userService.delete(target.getId());
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody UserDto dto) {
        UserEntity user = userService.show(dto.getId());

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null || user.getPw() != dto.getPw()) {
            return "로그인 아이디 또는 비밀번호가 틀렸습니다.";
        }

        // 로그인 성공 => Jwt Token 발급

        String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";
        long expireTimeMs = 1000 * 60 * 60;     // Token 유효 시간 = 60분

        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);

        return jwtToken;
    }
}
