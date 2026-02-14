package com.spring.ai.joseonannalapi.api.controller.v1.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank String code,
        @NotBlank String redirectUri
) {}
