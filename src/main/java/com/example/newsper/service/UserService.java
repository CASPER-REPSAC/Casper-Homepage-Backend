package com.example.newsper.service;

import com.example.newsper.constant.UserRole;
import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.util.JwtTokenUtil;
import com.example.newsper.repository.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Value("${custom.secret-key}")
    String secretKey;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private SubmitRepository submitRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity newUser(UserDto dto) {
        UserEntity userEntity = dto.toEntity();
        userEntity.setPw(passwordEncoder.encode(userEntity.getPw()));

        return userRepository.save(userEntity);
    }

    public UserEntity modify(UserEntity user) {
        return userRepository.save(user);
    }

    public UserEntity findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity findByName(String name) {
        return userRepository.findByName(name);
    }

    public Map<String, Object> showall(UserRole role) {
        List<UserEntity> users = userRepository.findAll();
        List<Map<String, Object>> userList = new ArrayList<>();
        Map<String, Object> target = new HashMap<>();
        for (UserEntity user : users)
            if (role == UserRole.GUEST || user.getRole() == role) userList.add(user.toJSON());
        target.put("memberList", userList);
        return target;
    }

    public UserEntity delete(String id) {
        UserEntity target = userRepository.findById(id).orElse(null);
        if (target == null)
            return null;
        userRepository.delete(target);
        return target;
    }

    public Map<String, Object> login(UserEntity user, HttpServletResponse response) {

        Map<String, Object> token = new HashMap<>();

        long expireTimeMs = 60 * 60 * 1000L; // Token 유효 시간 = 1시간 (밀리초 단위)
        long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000L; // Refresh Token 유효 시간 = 30일 (밀리초 단위)

        // JWT 생성
        String jwtToken = JwtTokenUtil.createToken(user.getId(), secretKey, expireTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken(user.getId(), secretKey, refreshExpireTimeMs);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // SSO로부터 날아온 JWT의 groups 정보 가져오기 & DB에 반영
        UserRole roleFromSSO = UserRole.extraUserRoleFromToken(jwtToken, secretKey);
        user.setRole(roleFromSSO);

        userRepository.save(user);

        // AccessToken 설정
        Cookie accessCookie = new Cookie("accessToken", jwtToken);
        accessCookie.setMaxAge((int) (expireTimeMs / 1000)); // 초 단위로 변경
        accessCookie.setSecure(true);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // RefreshToken 설정
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setMaxAge((int) (refreshExpireTimeMs / 1000)); // 초 단위로 변경
        refreshCookie.setSecure(true);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        token.put("accessToken", jwtToken);
        token.put("refreshToken", refreshToken);
        token.put("myInfo", user.toJSON());

        return token;
    }

    public void logout(UserEntity user, HttpServletResponse response) {

        user.setRefreshToken(null);
        userRepository.save(user);

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
    }

    public void changeNickname(UserEntity userEntity) {
        articleRepository.changeNicknameInArticle(userEntity.getNickname(), userEntity.getId());
        commentRepository.changeNicknameInComment(userEntity.getNickname(), userEntity.getId());
        assignmentRepository.changeNicknameInAssignment(userEntity.getNickname(), userEntity.getId());
        submitRepository.changeNicknameInSubmit(userEntity.getNickname(), userEntity.getId());
    }

    public void roleChange(UserEntity user, UserRole role) {
        user.setRole(role);
        userRepository.save(user);
    }

    public String getUserId(HttpServletRequest request) {
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return "guest";
            }
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION).replace("Bearer ", "");
            return JwtTokenUtil.getLoginId(accessToken, secretKey);
        } catch (Exception e) {
            log.info(e.getMessage());
            return "guest";
        }
    }
}
