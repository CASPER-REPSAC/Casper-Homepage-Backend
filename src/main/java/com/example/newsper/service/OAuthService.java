package com.example.newsper.service;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
public class OAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    String githubClientSecret;

    @Value("${spring.security.oauth2.client.registration.sso.client-id}")
    String ssoClientId;

    @Value("${spring.security.oauth2.client.registration.sso.client-secret}")
    String ssoClientSecret;
    @Autowired
    private UserService userService;

    public UserEntity google(String code, String redirectUri) {

        String accessToken = getGoogleAccessToken(code, redirectUri);
        log.info("AccessToken = " + accessToken);
        JsonNode userResourceNode = getGoogleUserResource(accessToken);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        log.info("email = " + email);

        if (userService.findByEmail(email) == null) {
            UserDto dto = new UserDto(email, id + email, email, name, email, null, null, null, "associate");
            return userService.newUser(dto);
        } else return userService.findByEmail(email);
    }

    private String getGoogleAccessToken(String authorizationCode, String redirectUri) {

        String tokenUri = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity entity = new HttpEntity(params, headers);

        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }

    private JsonNode getGoogleUserResource(String accessToken) {

        String resourceUri = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }

    public UserEntity github(String code, String redirectUri) {

        String accessToken = getGithubAccessToken(code, redirectUri);
        log.info("AccessToken = " + accessToken);
        JsonNode userResourceNode = getGithubUserResource(accessToken);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        log.info("email = " + email);

        if (userService.findByEmail(email) == null) {
            UserDto dto = new UserDto(email, id + email, email, name, email, null, null, null, "associate");
            return userService.newUser(dto);
        } else return userService.findByEmail(email);
    }

    private String getGithubAccessToken(String authorizationCode, String redirectUri) {

        String tokenUri = "https://github.com/login/oauth/access_token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", githubClientId);
        params.add("client_secret", githubClientSecret);
        params.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, JsonNode.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseBody = response.getBody();
            return responseBody != null ? responseBody.get("access_token").asText() : null;
        } else {
            throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
        }
    }

    private JsonNode getGithubUserResource(String accessToken) {

        String resourceUri = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }

    public UserEntity sso(String code, String redirectUri) {

        String accessToken = getSsoAccessToken(code, redirectUri);
        log.info("AccessToken = " + accessToken);
        JsonNode userResourceNode = getSsoUserResource(accessToken);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        log.info("email = " + email);

        if (userService.findByEmail(email) == null) {
            UserDto dto = new UserDto(email, id + email, email, name, email, null, null, null, "associate");
            return userService.newUser(dto);
        } else return userService.findByEmail(email);
    }

    private String getSsoAccessToken(String authorizationCode, String redirectUri) {
        String tokenUri = "https://sso.casper.or.kr/application/o/token/";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", ssoClientId);
        params.add("client_secret", ssoClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity entity = new HttpEntity(params, headers);

        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }

    private JsonNode getSsoUserResource(String accessToken) {
        String resourceUri = "https://sso.casper.or.kr/application/o/userinfo/";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }
}