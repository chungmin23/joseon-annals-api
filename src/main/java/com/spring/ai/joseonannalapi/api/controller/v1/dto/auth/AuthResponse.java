package com.spring.ai.joseonannalapi.api.controller.v1.dto.auth;

import com.spring.ai.joseonannalapi.domain.user.User;

public record AuthResponse(Long userId, String email, String nickname, String accessToken, String refreshToken) {

    public static AuthResponse of(User user, String accessToken, String refreshToken) {
        return new AuthResponse(user.userId(), user.email(), user.nickname(), accessToken, refreshToken);
    }
}
