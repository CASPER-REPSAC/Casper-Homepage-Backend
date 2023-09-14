package com.example.newsper.api;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> newUser(@RequestBody UserDto dto, BindingResult bindingResult){ //@RequestPart(value = "dto") UserDto dto, @RequestPart(value = "profile",required = false) MultipartFile imgFile
        try {
            Map<String, Object> ret = new HashMap<>();

            ret.put("id",dto.getId());
            ret.put("pw",dto.getPw());
            ret.put("email",dto.getEmail());
            ret.put("name",dto.getName());
            ret.put("nickname",dto.getNickname());

            UserEntity created;
//            if (!(imgFile == null)){
//                created = userService.newUser(dto,imgFile);
//            }
//            else{
//                created = userService.newUser(dto);
//            }
            created = userService.newUser(dto);
            return (created != null) ?
                    ResponseEntity.status(HttpStatus.CREATED).body(ret):
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
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto dto,HttpServletResponse response) {
        UserEntity user = userService.show(dto.getId());

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null || !(passwordEncoder.matches(dto.getPw(),user.getPw()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 로그인 성공 => Jwt Token 발급
        String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";
        long expireTimeMs = 1000 * 60 * 60;     // Token 유효 시간 = 60분
        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken();

        user.setRefreshToken(refreshToken);
        userService.modify(user);

        Map<String,Object> token = new HashMap<>();

        Cookie cookie = new Cookie("RefreshToken",refreshToken);
        cookie.setMaxAge(1000 * 60 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        token.put("AccessToken",jwtToken);
        token.put("RefreshToken",refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/logout")
    public void logout(@RequestParam String id){
        UserEntity user = userService.show(id);
        user.setRefreshToken(null);
        userService.modify(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response){
        String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";

        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
        if(!JwtTokenUtil.isExpired(accessToken,secretKey)) {
            String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
            UserEntity user = userService.show(userId);
            if(!JwtTokenUtil.isExpired(user.getRefreshToken(),secretKey)){

                long expireTimeMs = 1000 * 60 * 60;     // Token 유효 시간 = 60분
                String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);

                Map<String,Object> token = new HashMap<>();

                token.put("AccessToken",jwtToken);
                token.put("RefreshToken",user.getRefreshToken());
                Cookie cookie = new Cookie("RefreshToken",user.getRefreshToken());
                cookie.setMaxAge(1000 * 60 * 60);
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);

                return ResponseEntity.status(HttpStatus.OK).body(token);
            }

        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    }
}
