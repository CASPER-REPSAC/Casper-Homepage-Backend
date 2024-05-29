package com.example.newsper.api;

import java.io.IOException;
import com.example.newsper.dto.*;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.redis.RedisUtil;
import com.example.newsper.service.*;
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
import java.util.*;

@Tag(name= "User", description = "유저 API")
@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private FileService fileService;

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
    public ResponseEntity<?> join(@RequestBody JoinDto dto) {
        UserEntity user = userService.findById(dto.getId());
        UserDto userDto = dto.toUserDto();

        if(!mailService.verifyEmailCode(dto.getEmail(), dto.getEmailKey())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(setErrorCodeBody(-202));
        if(!dto.isValid()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-201));
        if(user != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(setErrorCodeBody(-203));

        userService.newUser(userDto);
        redisUtil.deleteData(dto.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "비밀번호 업데이트", description = "액세스 토큰 필요.")
    @PostMapping("/pwupdate")
    public ResponseEntity<?> pwReset(@Parameter(description = "새로운 비밀번호") @RequestParam String pw, HttpServletRequest request){
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);
        user.setPw(passwordEncoder.encode(pw));
        userService.modify(user);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "ID 찾기", description = "가입시 사용된 메일로 아이디를 찾습니다.")
    @PostMapping("/findid")
    public ResponseEntity<?> findid(@RequestBody findIdDto dto){
        UserEntity user = userService.findByEmail(dto.getEmail());
        if(user == null || !user.getName().equals(dto.getName()) || !user.getEmail().equals(dto.getEmail())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(setErrorCodeBody(-106));
        mailService.idMail(dto.getEmail(), user.getId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "비밀번호 찾기", description = "가입시 사용된 메일로 초기화된 비밀번호를 전송합니다.")
    @PostMapping("/findpw")
    public ResponseEntity<?> findpw(@RequestBody findPwDto dto){
        UserEntity user = userService.findByEmail(dto.getEmail());
        if(user == null || !user.getName().equals(dto.getName()) || !user.getEmail().equals(dto.getEmail()) || !user.getId().equals(dto.getId())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(setErrorCodeBody(-106));

        mailService.pwMail(user);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PostMapping("/update")
    @Operation(summary= "유저 정보 수정", description= "닉네임, 홈페이지 주소, 소개글, 프로필 파일을 수정합니다. 액세스 토큰 필요.")
    public ResponseEntity<UserEntity> update(@RequestPart(value = "userModifyDto") UserModifyDto dto, HttpServletRequest request, @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {
        String userId = userService.getUserId(request);
        UserEntity userEntity = userService.findById(userId);

        if(profile != null) {
            fileService.delete(userEntity.getProfileImgPath(),"profile");
            userEntity.setProfileImgPath(fileService.fileUpload(profile, "profile"));
        }

        userEntity.setNickname(dto.getNickname());
        userEntity.setHomepage(dto.getHomepage());
        userEntity.setIntroduce(dto.getIntroduce());

        return ResponseEntity.status(HttpStatus.OK).body(userService.modify(userEntity));
    }

    @DeleteMapping("/withdrawal/{id}")
    @Operation(summary= "회원 탈퇴", description= "본인, 관리자만 탈퇴 진행 가능합니다. 액세스 토큰 필요.")
    public ResponseEntity<UserEntity> userWithdrawal(@Parameter(description = "유저 ID") @PathVariable String id, HttpServletRequest request){
        String userId = userService.getUserId(request);
        UserEntity target = userService.findById(userId);

        if(target.getRole().equals("admin")) userService.delete(id);
        else if(target.getId().equals(id)) userService.delete(target.getId());
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();


        if(target.getProfileImgPath() != null) {
            fileService.delete(target.getProfileImgPath(),"profile");
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login")
    @Operation(summary= "로그인", description= "로그인 토큰을 반환합니다.")
    public ResponseEntity<?> login(@RequestBody LoginDto dto, HttpServletResponse response) {
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

        return ResponseEntity.status(HttpStatus.OK).body(userService.login(user,response));
    }

    @Operation(summary = "구글 로그인", description = "OAuth2를 사용하여 로그인 합니다.")
    @GetMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestParam String code, HttpServletResponse response) {
        log.info("googleCode : "+code);
        UserEntity user = oAuthService.socialLogin(code);
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(user,response));
    }

    @PostMapping("/logout")
    @Operation(summary= "로그아웃", description= "유저 토큰과 쿠키를 제거합니다. 액세스 토큰 필요.")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response){
        String userId = userService.getUserId(request);
        UserEntity user = userService.findById(userId);

        userService.logout(user, response);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/refresh")
    @Operation(summary= "리프레쉬", description= "유저 토큰과 쿠키를 재설정합니다. 액세스 토큰 필요.")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        for (Cookie c : cookies) {
            if (c.getName().equals("refreshToken") && !JwtTokenUtil.isExpired(c.getValue(), secretKey)) {
                String id = JwtTokenUtil.getLoginId(c.getValue(), secretKey);
                UserEntity user = userService.findById(id);
                return ResponseEntity.status(HttpStatus.OK).body(userService.login(user,response));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/show")
    @Operation(summary= "유저 정보 조회", description= "유저 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> show(@Parameter(description = "유저 ID") @RequestParam String id){
        log.info("show API input id ="+ id);
        UserEntity user = userService.findById(id);
        log.info("show API user");
        log.info(id);
        return ResponseEntity.status(HttpStatus.OK).body(user.toJSON());
    }

    @GetMapping("/showall")
    @Operation(summary= "유저 그룹 조회", description= "권한으로 유저 정보를 조회합니다.")
    public ResponseEntity<Map<String,Object>> showall(@Parameter(description ="associate, active, rest, graduate") @RequestParam String role){
        return ResponseEntity.status(HttpStatus.OK).body(userService.showall(role));
    }

    @PostMapping("/auth")
    @Operation(summary= "유저 권한 수정", description= "유저의 권한을 수정합니다. 액세스 토큰 필요.")
    public ResponseEntity auth(HttpServletRequest request, @Parameter(description = "associate, active, rest, graduate") @RequestBody RoleDto dto){
        String userId = userService.getUserId(request);

        if(!userId.equals("admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if(dto.getRole().equals("admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if(dto.getId().equals("admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserEntity user = userService.findById(dto.getId());
        userService.roleChange(user, dto.getRole());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private Map<String, Object> setErrorCodeBody(int code){
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", code);
        return responseBody;
    }
}
