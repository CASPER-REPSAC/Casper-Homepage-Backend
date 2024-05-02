package com.example.newsper.api;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.example.newsper.dto.*;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.redis.RedisUtil;
import com.example.newsper.service.AccountLockService;
import com.example.newsper.service.MailService;
import com.example.newsper.service.OAuthService;
import com.example.newsper.service.UserService;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.*;

@Tag(name= "User", description = "유저 API")
@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private AccountLockService accountLockService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OAuthService oAuthService;

    @Value("${custom.secret-key}")
    String secretKey;

    @PostMapping("/join")
    @Operation(summary= "회원 가입", description= "DB에 회원 정보를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    public ResponseEntity<?> join(
            @Parameter(description = "Content-type:application/json, 파라미터 명: joinDto")
            @RequestPart(value = "joinDto") JoinDto dto,
            @Parameter(description = "Content-type:multipart/form-data, 파라미터 명: profile")
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {

        Map<String, Object> ret = new HashMap<>();

        if(!mailService.verifyEmailCode(dto.getEmail(), dto.getEmailKey()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(setErrorCodeBody(-202));

        if(dto.getId() == null ||
                dto.getPw() == null ||
                dto.getEmail() == null ||
                dto.getName() == null ||
                dto.getNickname() == null ||
                dto.getEmailKey() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-201));

        UserEntity user = userService.findById(dto.getId());

        if(user != null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-203));

        UserDto userDto = dto.toUserDto(dto);

        if(profile != null) {
            File checkfile = new File(profile.getOriginalFilename());
            String type = null;

            try {
                type = Files.probeContentType(checkfile.toPath());
                log.info("MIME TYPE : " + type);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!type.startsWith("image") || profile.getSize() > 10485760) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            String uploadFolder = "/home/casper/newsper_profile";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date date = new Date();
            String str = sdf.format(date);
            String datePath = str.replace("-", File.separator);

            File uploadPath = new File(uploadFolder, datePath);

            if (uploadPath.exists() == false) {
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

            String serverUrl = "http://build.casper.or.kr";
            String profileUrl = serverUrl + "/profile/" + datePath + "/" + uploadFileName;

            userDto.setProfileImgPath(profileUrl);
            ret.put("profile",userDto.getProfileImgPath());
        }

        ret.put("id",userDto.getId());
        ret.put("email",userDto.getEmail());
        ret.put("name",userDto.getName());
        ret.put("nickname",userDto.getNickname());

        UserEntity created = userService.newUser(userDto);

        ret.put("pw",created.getPw());

        redisUtil.deleteData(dto.getEmail());

        return (created != null) ?
                ResponseEntity.status(HttpStatus.CREATED).body(ret):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/update")
    @Operation(summary= "유저 정보 수정", description= "닉네임, 홈페이지 주소, 소개글, 프로필 파일을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    public ResponseEntity<UserEntity> update(
            @Parameter(description = "profile API를 통해 프로필 주소를 받아와서 사용합니다.")
            @RequestBody UserModifyDto dto, HttpServletRequest request
    ){
        String userId = getUserId(request);
        UserEntity userEntity = userService.findById(userId);
        userEntity.setNickname(dto.getNickname());
        userEntity.setHomepage(dto.getHomepage());
        userEntity.setIntroduce(dto.getIntroduce());
        userEntity.setProfileImgPath(dto.getProfileImgPath());

        return ResponseEntity.status(HttpStatus.OK).body(userService.modify(userEntity));
    }

    @DeleteMapping("/withdrawal/{id}")
    @Operation(summary= "회원 탈퇴", description= "본인, 관리자만 탈퇴 진행 가능합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "관리자는 탈퇴가 불가능합니다.")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<UserEntity> userWithdrawal(
            @Parameter(description = "유저 ID")
            @PathVariable String id,
            HttpServletRequest request
    ){
        UserEntity target;
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
            target = userService.findById(userId);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(id.equals("admin")) return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();

        if(target.getRole().equals("admin")) {
            userService.delete(id);
        }

        else {
            if(target.getId().equals(id)) userService.delete(target.getId());
            else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login")
    @Operation(summary= "로그인", description= "로그인 토큰을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity<?> login(
            @Parameter(description = "id, pw")
            @RequestBody LoginDto dto, HttpServletResponse response) {
        UserEntity user = userService.findById(dto.getId());

        if(accountLockService.validation(dto.getId())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-105));

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null) {
            accountLockService.setCount(dto.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-101));
        }
        if(!(passwordEncoder.matches(dto.getPw(),user.getPw()))) {
            accountLockService.setCount(dto.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-102));
        }

        accountLockService.deleteCount(dto.getId());

        // 로그인 성공 => Jwt Token 발급
        long expireTimeMs = 60 * 60 * 1000L; // Token 유효 시간 = 1시간 (밀리초 단위)
        long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000L; // Refresh Token 유효 시간 = 30일 (밀리초 단위)

        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken(user.getId(), secretKey, refreshExpireTimeMs);

        Date expiredDate1 = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken).getBody().getExpiration();
        log.info("jwtToken : " + expiredDate1.toString());
        Date expiredDate = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(refreshToken).getBody().getExpiration();
        log.info("refreshToken : " + expiredDate.toString());

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

    @GetMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestParam String code, HttpServletResponse response) {
        System.out.println("Received authorization code: " + code);
        UserEntity user = oAuthService.socialLogin(code);

        // 로그인 성공 => Jwt Token 발급
        long expireTimeMs = 60 * 60 * 1000L; // Token 유효 시간 = 1시간 (밀리초 단위)
        long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000L; // Refresh Token 유효 시간 = 30일 (밀리초 단위)

        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken(user.getId(), secretKey, refreshExpireTimeMs);

        Date expiredDate1 = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken).getBody().getExpiration();
        log.info("jwtToken : " + expiredDate1.toString());
        Date expiredDate = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(refreshToken).getBody().getExpiration();
        log.info("refreshToken : " + expiredDate.toString());

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
    @Operation(summary= "로그아웃", description= "유저 토큰과 쿠키를 제거합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "파라미터 오류")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response){
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
            String userId = JwtTokenUtil.getLoginId(accessToken, secretKey);
            UserEntity user = userService.findById(userId);
            user.setRefreshToken(null);
            userService.modify(user);

            Cookie refreshCookie = new Cookie("refreshToken", null);
            refreshCookie.setMaxAge(0);
            refreshCookie.setSecure(true);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            Cookie accessCookie = new Cookie("accessToken", null);
            accessCookie.setMaxAge(0);
            accessCookie.setSecure(true);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            response.addCookie(accessCookie);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary= "리프레쉬", description= "유저 토큰과 쿠키를 재설정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "토큰이 없거나 만료되었습니다.")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response){
        try {
            Cookie[] cookies = request.getCookies();
            for (Cookie c : cookies) {
                if (c.getName().equals("refreshToken") && !JwtTokenUtil.isExpired(c.getValue(), secretKey)) {
                    String id = JwtTokenUtil.getLoginId(c.getValue(), secretKey);

                    // AccessToken 만료 시간 = 1시간 (밀리초 단위)
                    long expireTimeMs = 60 * 60 * 1000L;

                    // RefreshToken 만료 시간 = 30일 (밀리초 단위)
                    long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000L;

                    String jwtToken = JwtTokenUtil.createToken(id, secretKey, expireTimeMs);
                    String refreshToken = JwtTokenUtil.createRefreshToken(id, secretKey, refreshExpireTimeMs);

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

                    UserEntity user = userService.findById(id);
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
    @Operation(summary= "유저 정보 조회", description= "유저 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "유저 정보를 찾을 수 없습니다.")
    public ResponseEntity<Map<String, Object>> show(
            @Parameter(description = "유저 ID")
            @RequestParam String id
    ){
        try{
            UserEntity user = userService.findById(id);
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
    @Operation(summary= "유저 그룹 조회", description= "권한으로 유저 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "유저 정보를 찾을 수 없습니다.")
    public ResponseEntity<Map<String,Object>> showall(
            @Parameter(description = " all, associate, active, rest, graduate")
            @RequestParam String role
    ){
        try {
            List<UserEntity> users = userService.showall();
            List<Map<String, Object>> userList = new ArrayList<>();
            Map<String, Object> target = new HashMap<>();
            for (UserEntity user : users) {
                if(user.getRole().equals(role)||role.equals("all")){
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping("/auth")
    @Operation(summary= "유저 권한 수정", description= "유저의 권한을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "권한이 없습니다.")
    public ResponseEntity auth(
            HttpServletRequest request,
            @Parameter(description = " all, associate, active, rest, graduate")
            @RequestBody RoleDto dto
    ){
        String userId = getUserId(request);
        if(!userId.equals("admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-1));

        UserEntity user = userService.findById(dto.getId());
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
