package com.example.newsper.api;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.repository.UserRepository;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public ResponseEntity<?> newUser(@RequestBody UserDto dto, BindingResult bindingResult){ //@RequestPart(value = "dto") UserDto dto, @RequestPart(value = "profile",required = false) MultipartFile imgFile
        Map<String, Object> ret = new HashMap<>();

        if(dto.getId() == null || dto.getPw() == null || dto.getEmail() == null || dto.getName() == null || dto.getNickname() == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-201));

        UserEntity user = userService.show(dto.getId());
        if(user != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-203));

        ret.put("id",dto.getId());
        ret.put("pw",dto.getPw());
        ret.put("email",dto.getEmail());
        ret.put("name",dto.getName());
        ret.put("nickname",dto.getNickname());


//            if (!(imgFile == null)){
//                created = userService.newUser(dto,imgFile);
//            }
//            else{
//                created = userService.newUser(dto);
//            }
        UserEntity created = userService.newUser(dto);

        return (created != null) ?
                ResponseEntity.status(HttpStatus.CREATED).body(ret):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/update")
    public ResponseEntity<UserEntity> update(@RequestBody UserDto dto, HttpServletRequest request){
        String userId = getUserId(request);
        UserEntity userEntity = userService.show(userId);
        userEntity.setPw(dto.getPw());
        userEntity.setNickname(dto.getNickname());
        userEntity.setHomepage(dto.getHomepage());
        userEntity.setIntroduce(dto.getIntroduce());
        userEntity.setProfileImgPath(dto.getProfileImgPath());

        return ResponseEntity.status(HttpStatus.OK).body(userService.modify(userEntity));
    }

    @DeleteMapping("/withdrawal/{tarId}")
    public ResponseEntity<UserEntity> userWithdrawal(HttpServletRequest request, @PathVariable String tarId){
        UserEntity target;
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
            target = userService.show(userId);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(target.getRole().equals("admin")) {
            userService.delete(tarId);
        }

        else {
            userService.delete(target.getId());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto dto,HttpServletResponse response) {
        UserEntity user = userService.show(dto.getId());

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-101));
        if(!(passwordEncoder.matches(dto.getPw(),user.getPw()))) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-102));

        // 로그인 성공 => Jwt Token 발급

        long expireTimeMs = 60;     // Token 유효 시간 = 1분
        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs*3);
        String refreshToken = JwtTokenUtil.createRefreshToken(user.getId(), secretKey, expireTimeMs*5);

        user.setRefreshToken(refreshToken);
        userService.modify(user);

        Map<String,Object> token = new HashMap<>();

        Cookie refreshCookie = new Cookie("refreshToken",refreshToken);
        refreshCookie.setMaxAge((int) (expireTimeMs*5));
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        Cookie accessCookie = new Cookie("accessToken",jwtToken);
        accessCookie.setMaxAge((int) (expireTimeMs*3));
        accessCookie.setSecure(true);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        token.put("accessToken",jwtToken);
        token.put("refreshToken",refreshToken);

        Map<String, Object> map = new HashMap<>();
        map.put("role", user.getRole());
        map.put("name", user.getName());
        map.put("nickname", user.getNickname());
        map.put("email", user.getEmail());
        map.put("introduce", user.getIntroduce());
        map.put("id", user.getId());
        map.put("image", user.getProfileImgPath());
        map.put("homepage", user.getHomepage());
        token.put("myInfo",map);

        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response){
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
        String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
        UserEntity user = userService.show(userId);
        user.setRefreshToken(null);
        userService.modify(user);

        Cookie refreshCookie = new Cookie("refreshToken",null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        Cookie accessCookie = new Cookie("accessToken",null);
        accessCookie.setMaxAge(0);
        accessCookie.setSecure(true);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        try {
            for (Cookie c : cookies) {
                if (c.getName().equals("refreshToken") || !JwtTokenUtil.isExpired(c.getValue(), secretKey)) {
                    long expireTimeMs = 60 * 5;     // Token 유효 시간 = 5분
                    log.info(c.getValue());
                    String id = JwtTokenUtil.getLoginId(c.getValue(), secretKey);
                    String jwtToken = JwtTokenUtil.createToken(id, secretKey, expireTimeMs);

                    Map<String, Object> token = new HashMap<>();

                    token.put("accessToken", jwtToken);

                    Cookie refreshCookie = new Cookie("refreshToken", c.getValue());
                    refreshCookie.setMaxAge(60 * 5);
                    refreshCookie.setSecure(true);
                    refreshCookie.setHttpOnly(true);
                    refreshCookie.setPath("/");
                    response.addCookie(refreshCookie);

                    UserEntity user = userService.show(id);

                    Map<String, Object> map = new HashMap<>();
                    map.put("role", user.getRole());
                    map.put("name", user.getName());
                    map.put("nickname", user.getNickname());
                    map.put("email", user.getEmail());
                    map.put("introduce", user.getIntroduce());
                    map.put("id", user.getId());
                    map.put("image", user.getProfileImgPath());
                    map.put("homepage", user.getHomepage());
                    token.put("myInfo",map);

                    return ResponseEntity.status(HttpStatus.OK).body(token);
                }
            }
        } catch (Exception e){
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/show")
    public ResponseEntity<Map<String, Object>> show(@RequestParam String id){
        try{
            UserEntity user = userService.show(id);
            Map<String, Object> map = new HashMap<>();
            map.put("role", user.getRole());
            map.put("name", user.getName());
            map.put("nickname", user.getNickname());
            map.put("email", user.getEmail());
            map.put("introduce", user.getIntroduce());
            map.put("id", user.getId());
            map.put("image", user.getProfileImgPath());
            map.put("homepage", user.getHomepage());
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/showall")
    public ResponseEntity<Map<String,Object>> showall(@RequestParam String role){
        try {
            List<UserEntity> users = userService.showall();
            List<Map<String, Object>> userList = new ArrayList<>();
            Map<String, Object> target = new HashMap<>();
            for (UserEntity user : users) {
                if(user.getRole().equals(role)){
                    Map<String, Object> map = new HashMap<>();
                    map.put("role", user.getRole());
                    map.put("name", user.getName());
                    map.put("nickname", user.getNickname());
                    map.put("email", user.getEmail());
                    map.put("introduce", user.getIntroduce());
                    map.put("id", user.getId());
                    map.put("image", user.getProfileImgPath());
                    map.put("homepage", user.getHomepage());
                    userList.add(map);
                }
            }
            target.put("memberList",userList);
            return ResponseEntity.status(HttpStatus.OK).body(target);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    private String getUserId(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            return JwtTokenUtil.getLoginId(accessToken, secretKey);
        } catch(Exception e){
            return "guest";
        }
    }

    private Map<String, Object> setErrorCodeBody(int code){
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpStatus.UNAUTHORIZED.value());
        responseBody.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        responseBody.put("code", code);
        return responseBody;
    }
}
