package com.spring.ai.joseonannalapi.api.controller.v1.dto.content;

import jakarta.validation.constraints.NotNull;

public record SaveLibraryRequest(@NotNull Long contentId) {
}
