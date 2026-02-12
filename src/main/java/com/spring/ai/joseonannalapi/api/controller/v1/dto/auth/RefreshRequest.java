package com.spring.ai.joseonannalapi.api.controller.v1.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {
}
