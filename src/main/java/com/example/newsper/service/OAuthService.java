package com.example.newsper.service;

import com.example.newsper.constant.UserRole;
import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OAuthService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserService userService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    public UserEntity google(String code) {
        ClientRegistration google = clientRegistrationRepository.findByRegistrationId("google");
        JsonNode userResourceNode = getUserResource(issueToken(code, google),
                google.getProviderDetails());
        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        return getUser(id, email, name);
    }

    public UserEntity github(String code) {
        ClientRegistration github = clientRegistrationRepository.findByRegistrationId("github");
        JsonNode userResourceNode = getUserResource(issueToken(code, github),
                github.getProviderDetails());
        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        return getUser(id, email, name);
    }

    public UserEntity sso(String code) {
        ClientRegistration sso = clientRegistrationRepository.findByRegistrationId("sso");
        JsonNode userResourceNode = getUserResource(issueToken(code, sso),
                sso.getProviderDetails());
        String id = userResourceNode.get("nickname").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();

        if (userService.findByName(name) != null) {
            UserEntity entity = userService.findByName(name);
            entity.setEmail(email);
            userService.modify(entity);
        }

        // revert: see code before commit 1e2fbd6
        UserEntity user = getUser(id, email, name, true);
        UserRole role = user.getRole();
        ArrayList<String> groups = new ArrayList<>();
        userResourceNode.get("groups").elements().forEachRemaining((JsonNode node) -> groups.add(node.asText()));
        UserRole ssoRole = getSsoUserRole(groups);
        if (role != ssoRole) {
            user.setRole(ssoRole);
            userService.modify(user);
        }
        return user;
    }

    private String issueToken(String authorizationCode, ClientRegistration client) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client.getClientId());
        params.add("client_secret", client.getClientSecret());
        params.add("redirect_uri", client.getRedirectUri());
        params.add("code", authorizationCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(params, headers);

        ResponseEntity<JsonNode> responseNode =
                restTemplate.exchange(client.getProviderDetails().getTokenUri(), HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        try {
            assert accessTokenNode != null;
            return accessTokenNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Failed to get access token: {}", responseNode.getStatusCode());
            return null;
        }
    }

    private JsonNode getUserResource(String accessToken, ClientRegistration.ProviderDetails provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(provider.getUserInfoEndpoint().getUri(), HttpMethod.GET, entity, JsonNode.class).getBody();
    }

    @Deprecated(forRemoval = true)
    private UserEntity getUser(String id, String email, String name) {
        return getUser(id, email, name, false);
    }

    // revert: see code before commit 1e2fbd6
    private UserEntity getUser(String id, String email, String name, boolean enableRegister) {
        UserEntity user = userService.findByEmail(email);
        if (enableRegister && user == null) {
            String password = id + email;
            UserDto dto = new UserDto();
            dto.setId(email);
            dto.setPw(password);
            dto.setEmail(email);
            dto.setName(name);
            dto.setNickname(email);
            dto.setRole(UserRole.ASSOCIATE.getRole());
            user = userService.newUser(dto);
        }
        return user;
    }

    private UserRole getSsoUserRole(List<String> roles) {
        var roleMap = Map.of(
                "관리자", UserRole.ADMIN,
                "활동중", UserRole.ACTIVE,
                "졸업생", UserRole.GRADUATE,
                "비활동", UserRole.REST
        );
        return roles.stream()
                .filter(roleMap::containsKey)
                .findFirst()
                .map(roleMap::get)
                .orElse(UserRole.ASSOCIATE);
    }
}