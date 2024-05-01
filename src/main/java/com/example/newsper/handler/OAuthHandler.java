package com.example.newsper.handler;

import com.example.newsper.entity.UserEntity;
import com.example.newsper.jwt.JwtTokenUtil;
import com.example.newsper.repository.UserRepository;
import com.example.newsper.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${custom.secret-key}")
    String secretKey;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User로 캐스팅하여 인증된 사용자 정보를 가져온다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 사용자 이메일을 가져온다.
        String email = oAuth2User.getAttribute("email");
        // 서비스 제공 플랫폼(GOOGLE, KAKAO, NAVER)이 어디인지 가져온다.
        String provider = oAuth2User.getAttribute("provider");

        // CustomOAuth2UserService에서 셋팅한 로그인한 회원 존재 여부를 가져온다.
        boolean isExist = oAuth2User.getAttribute("exist");
        // OAuth2User로 부터 Role을 얻어온다.
        String role = oAuth2User.getAuthorities().stream().
                findFirst() // 첫번째 Role을 찾아온다.
                .orElseThrow(IllegalAccessError::new) // 존재하지 않을 시 예외를 던진다.
                .getAuthority(); // Role을 가져온다.

        // 회원이 존재할경우
        if (isExist) {

            // 회원이 존재하면 jwt token 발행을 시작한다.
            long expireTimeMs = 60 * 60 * 1000L; // Token 유효 시간 = 1시간 (밀리초 단위)
            long refreshExpireTimeMs = 30 * 24 * 60 * 60 * 1000L; // Refresh Token 유효 시간 = 30일 (밀리초 단위)

            String jwtToken = JwtTokenUtil.createToken(email, secretKey, expireTimeMs);
            String refreshToken = JwtTokenUtil.createRefreshToken(email, secretKey, refreshExpireTimeMs);

            UserEntity user = userService.findByEmail(email);
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

        } else {

            // 회원이 존재하지 않을경우, 서비스 제공자와 email을 쿼리스트링으로 전달하는 url을 만들어준다.
            String targetUrl = UriComponentsBuilder.fromUriString("http://3.39.72.204/loginSuccess")
                    .queryParam("email", (String) oAuth2User.getAttribute("email"))
                    .queryParam("provider", provider)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
            // 회원가입 페이지로 리다이렉트 시킨다.
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

}
