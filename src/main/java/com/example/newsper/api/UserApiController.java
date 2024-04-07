package com.example.newsper.api;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.text.SimpleDateFormat;
import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> newUser(@RequestBody UserDto dto){
        Map<String, Object> ret = new HashMap<>();

        if(dto.getId() == null || dto.getPw() == null || dto.getEmail() == null || dto.getName() == null || dto.getNickname() == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-201));

        UserEntity user = userService.show(dto.getId());
        if(user != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-203));

        ret.put("id",dto.getId());
        ret.put("pw",dto.getPw());
        ret.put("email",dto.getEmail());
        ret.put("name",dto.getName());
        ret.put("nickname",dto.getNickname());
        ret.put("profile",dto.getProfileImgPath());

        UserEntity created = userService.newUser(dto);

        return (created != null) ?
                ResponseEntity.status(HttpStatus.CREATED).body(ret):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/image")
    public ResponseEntity<?> update(@RequestPart(value = "profile") MultipartFile profile) throws IOException {
//        log.info("파일 이름 : " + profile.getOriginalFilename());
//        log.info("파일 타입 : " + profile.getContentType());
//        log.info("파일 크기 : " + profile.getSize());

        File checkfile = new File(profile.getOriginalFilename());
        String type = null;

        try {
            type = Files.probeContentType(checkfile.toPath());
            log.info("MIME TYPE : " + type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!type.startsWith("image")||profile.getSize()>1048576) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String uploadFolder = "/home/casper/newsper_profile";
//        String uploadFolder = "C:\\Users\\koko9\\Downloads";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = new Date();
        String str = sdf.format(date);
        String datePath = str.replace("-", File.separator);

        File uploadPath = new File(uploadFolder, datePath);

        if(uploadPath.exists() == false) {
            uploadPath.mkdirs();
        }

        /* 파일 이름 */
        String uploadFileName = profile.getOriginalFilename();

        /* UUID 설정 */
        String uuid = UUID.randomUUID().toString();
        uploadFileName = uuid + "_" + uploadFileName;

        /* 파일 위치, 파일 이름을 합친 File 객체 */
        File saveFile = new File(uploadPath, uploadFileName);

        profile.transferTo(saveFile);

        return ResponseEntity.status(HttpStatus.OK).body("{ \"profile\" : \""+saveFile.getAbsolutePath()+"\"}");
    }

    @PostMapping("/update")
    public ResponseEntity<UserEntity> update(@RequestBody UserDto dto, HttpServletRequest request){
        String userId = getUserId(request);
        UserEntity userEntity = userService.show(userId);
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
    public ResponseEntity<?> login(@RequestBody UserDto dto, HttpServletResponse response) {
        UserEntity user = userService.show(dto.getId());

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-101));
        if(!(passwordEncoder.matches(dto.getPw(),user.getPw()))) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-102));

        // 로그인 성공 => Jwt Token 발급
        long currentTimeMs = System.currentTimeMillis();
        long expireTimeMs = 60 * 60 * 1000; // Token 유효 시간 = 1시간 (밀리초 단위)
        long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000; // Refresh Token 유효 시간 = 30일 (밀리초 단위)

        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, currentTimeMs+expireTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken(user.getId(), secretKey, currentTimeMs+refreshExpireTimeMs);

        user.setRefreshToken(refreshToken);
        userService.modify(user);

        Map<String,Object> token = new HashMap<>();

        // AccessToken 설정
        Cookie accessCookie = new Cookie("accessToken",jwtToken);
        accessCookie.setMaxAge((int) (expireTimeMs / 1000)); // 초 단위로 변경
        accessCookie.setSecure(true);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // RefreshToken 설정
        Cookie refreshCookie = new Cookie("refreshToken",refreshToken);
        refreshCookie.setMaxAge((int) (refreshExpireTimeMs / 1000)); // 초 단위로 변경
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

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
                if (c.getName().equals("refreshToken") && !JwtTokenUtil.isExpired(c.getValue(), secretKey)) {
                    String id = JwtTokenUtil.getLoginId(c.getValue(), secretKey);

                    long currentTimeMs = System.currentTimeMillis();

                    // AccessToken 만료 시간 = 1시간 (밀리초 단위)
                    long expireTimeMs = 60 * 60 * 1000;

                    // RefreshToken 만료 시간 = 30일 (밀리초 단위)
                    long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000;

                    String jwtToken = JwtTokenUtil.createToken(id, secretKey, currentTimeMs+expireTimeMs);
                    String refreshToken = JwtTokenUtil.createRefreshToken(id, secretKey, currentTimeMs+refreshExpireTimeMs);

                    Map<String, Object> token = new HashMap<>();

                    token.put("accessToken", jwtToken);

                    // RefreshToken 설정
                    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
                    refreshCookie.setMaxAge((int) (refreshExpireTimeMs / 1000)); // 초 단위로 변경
                    refreshCookie.setSecure(true);
                    refreshCookie.setHttpOnly(true);
                    refreshCookie.setPath("/");
                    response.addCookie(refreshCookie);

                    // AccessToken 설정
                    Cookie accessCookie = new Cookie("accessToken", jwtToken);
                    accessCookie.setMaxAge((int) (expireTimeMs / 1000)); // 초 단위로 변경
                    accessCookie.setSecure(true);
                    accessCookie.setHttpOnly(true);
                    accessCookie.setPath("/");
                    response.addCookie(accessCookie);

                    UserEntity user = userService.show(id);
                    user.setRefreshToken(refreshToken);
                    userService.modify(user);

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

    @PostMapping("/auth")
    public ResponseEntity auth(HttpServletRequest request, @RequestBody UserDto dto) {
        String userId = getUserId(request);
        if(!userId.equals("admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-1));

        UserEntity user = userService.show(dto.getId());
        user.setRole(dto.getRole());
        userService.modify(user);
        return ResponseEntity.status(HttpStatus.OK).build();
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
        responseBody.put("code", code);
        return responseBody;
    }
}
