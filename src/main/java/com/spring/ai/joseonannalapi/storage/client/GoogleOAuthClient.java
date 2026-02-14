package com.spring.ai.joseonannalapi.storage.client;

import com.spring.ai.joseonannalapi.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
        if (isBlank(clientId) || isBlank(clientSecret)) {
            throw new BusinessException("GOOGLE_AUTH_CONFIG_MISSING", "Google auth config is missing.");
        }
        if (isBlank(code) || isBlank(redirectUri)) {
            throw new BusinessException("GOOGLE_INVALID_REQUEST", "Google login request is invalid.");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            tokenResponse = response;
        } catch (WebClientResponseException e) {
            throw new BusinessException(
                    "GOOGLE_TOKEN_EXCHANGE_FAILED",
                    "Google token exchange failed: " + summarizeBody(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            throw new BusinessException("GOOGLE_TOKEN_EXCHANGE_FAILED", "Google token exchange failed.");
        }

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new BusinessException("GOOGLE_TOKEN_EXCHANGE_FAILED", "Google access_token is missing in response.");
        }
        String accessToken = (String) tokenResponse.get("access_token");

        Map<String, Object> userInfo;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri(USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            userInfo = response;
        } catch (WebClientResponseException e) {
            throw new BusinessException(
                    "GOOGLE_USERINFO_FAILED",
                    "Google user info request failed: " + summarizeBody(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            throw new BusinessException("GOOGLE_USERINFO_FAILED", "Google user info request failed.");
        }

        if (userInfo == null || !userInfo.containsKey("email")) {
            throw new BusinessException("GOOGLE_USERINFO_FAILED", "Google user info response has no email.");
        }

        return new GoogleUserInfo(
                (String) userInfo.get("email"),
                (String) userInfo.getOrDefault("name", "Google User"),
                (String) userInfo.getOrDefault("picture", "")
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String summarizeBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "empty response body";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() > 250 ? normalized.substring(0, 250) + "..." : normalized;
    }

    public record GoogleUserInfo(String email, String name, String picture) {
    }
}
