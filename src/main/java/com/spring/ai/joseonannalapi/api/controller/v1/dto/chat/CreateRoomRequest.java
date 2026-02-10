package com.spring.ai.joseonannalapi.api.controller.v1.dto.chat;

import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(@NotNull Long personaId) {
}
