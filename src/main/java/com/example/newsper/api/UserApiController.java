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

    String secretKey = "mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123mysecretkey123123";

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

    @DeleteMapping("/withdrawal/{tarId}")
    public ResponseEntity<UserEntity> userWithdrawal(HttpServletRequest request, @PathVariable String tarId){
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
        String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
        UserEntity target = userService.show(userId);

        if(target.getRole().equals("관리자")) {
            userService.delete(tarId);
        }

        else {
            userService.delete(target.getId());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto dto,HttpServletResponse response) {
        UserEntity user = userService.show(dto.getId());

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null || !(passwordEncoder.matches(dto.getPw(),user.getPw()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 로그인 성공 => Jwt Token 발급

        long expireTimeMs = 1000 * 60 * 60;     // Token 유효 시간 = 60분
        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken();

        user.setRefreshToken(refreshToken);
        userService.modify(user);

        Map<String,Object> token = new HashMap<>();

        Cookie refreshCookie = new Cookie("refreshToken",refreshToken);
        refreshCookie.setMaxAge(1000 * 60 * 60);
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        Cookie accessCookie = new Cookie("accessToken",jwtToken);
        accessCookie.setMaxAge(1000 * 60 * 60);
        accessCookie.setSecure(true);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        token.put("accessToken",jwtToken);
        token.put("refreshToken",refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response){
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
        String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
        UserEntity user = userService.show(userId);
        user.setRefreshToken(null);
        userService.modify(user);

        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(int i=0; i< cookies.length; i++){
                cookies[i].setMaxAge(0); // 유효시간을 0으로 설정
                response.addCookie(cookies[i]); // 응답 헤더에 추가
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response){
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
        if(!JwtTokenUtil.isExpired(accessToken,secretKey)) {
            String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
            UserEntity user = userService.show(userId);
            if(!JwtTokenUtil.isExpired(user.getRefreshToken(),secretKey)){

                long expireTimeMs = 1000 * 60 * 60;     // Token 유효 시간 = 60분
                String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);

                Map<String,Object> token = new HashMap<>();

                token.put("accessToken",jwtToken);
                token.put("refreshToken",user.getRefreshToken());

                Cookie refreshCookie = new Cookie("refreshToken",user.getRefreshToken());
                refreshCookie.setMaxAge(1000 * 60 * 60);
                refreshCookie.setSecure(true);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setPath("/");
                response.addCookie(refreshCookie);

                Cookie accessCookie = new Cookie("accessToken",jwtToken);
                accessCookie.setMaxAge(1000 * 60 * 60);
                accessCookie.setSecure(true);
                accessCookie.setHttpOnly(true);
                accessCookie.setPath("/");
                response.addCookie(accessCookie);

                return ResponseEntity.status(HttpStatus.OK).body(token);
            }

        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<Map<String, Object>> show(@PathVariable String id){
        try{
            UserEntity user = userService.show(id);
            Map<String, Object> map = new HashMap<>();
            map.put("name",user.getName());
            map.put("nickname",user.getNickname());
            map.put("email",user.getEmail());
            map.put("role",user.getRole());
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
