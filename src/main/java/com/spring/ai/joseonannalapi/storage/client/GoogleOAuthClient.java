package com.spring.ai.joseonannalapi.storage.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class GoogleOAuthClient {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    private final WebClient webClient = WebClient.create();

    public GoogleUserInfo getUserInfo(String code, String redirectUri) {
        // 1. 인증 코드 → 액세스 토큰 교환
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Google 토큰 발급 실패");
        }
        String accessToken = (String) tokenResponse.get("access_token");

        // 2. 액세스 토큰 → 사용자 정보 조회
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = webClient.get()
                .uri(USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (userInfo == null || !userInfo.containsKey("email")) {
            throw new RuntimeException("Google 사용자 정보 조회 실패");
        }

        return new GoogleUserInfo(
                (String) userInfo.get("email"),
                (String) userInfo.getOrDefault("name", "구글사용자"),
                (String) userInfo.getOrDefault("picture", "")
        );
    }

    public record GoogleUserInfo(String email, String name, String picture) {}
}
