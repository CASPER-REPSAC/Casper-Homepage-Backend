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

@Slf4j
@Service
public class OAuthService {

    @Autowired
    private UserService userService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    String githubClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public UserEntity google(String code, String redirectUri) {

        String accessToken = getGoogleAccessToken(code, redirectUri);
        JsonNode userResourceNode = getGoogleUserResource(accessToken);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        log.info("email = "+email);

        if(userService.findById(email) == null){
            UserDto dto = new UserDto(email,id+email,email, email, email,null,null,null,"associate");
            return userService.newUser(dto);
        } else return userService.findById(email);
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

//    public UserEntity github(String code, String redirectUri) {
//
//        String accessToken = getAccessToken(code, redirectUri);
//        JsonNode userResourceNode = getUserResource(accessToken);
//
//        String id = userResourceNode.get("id").asText();
//        String email = userResourceNode.get("email").asText();
//        log.info("email = "+email);
//
//        if(userService.findById(email) == null){
//            UserDto dto = new UserDto(email,id+email,email, email, email,null,null,null,"associate");
//            return userService.newUser(dto);
//        } else return userService.findById(email);
//    }

    public UserEntity github(String code, String redirectUri) {

        String accessToken = getGithubAccessToken(code, redirectUri);
        JsonNode userResourceNode = getGithubUserResource(accessToken);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        log.info("email = "+email);

        if(userService.findById(email) == null){
            UserDto dto = new UserDto(email,id+email,email, email, email,null,null,null,"associate");
            return userService.newUser(dto);
        } else return userService.findById(email);
    }

    private String getGithubAccessToken(String authorizationCode, String redirectUri) {

        String tokenUri = "https://github/login/oauth/access_token";

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

    private JsonNode getGithubUserResource(String accessToken) {

        String resourceUri = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }
}