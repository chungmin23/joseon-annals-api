package com.spring.ai.joseonannalapi.api.controller.v1.dto.user;

import com.spring.ai.joseonannalapi.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

public record UserStatsResponse(
        Long totalMessages,
        Long totalConversations,
        MostChattedPersonaDto mostChattedPersona,
        List<RecentPersonaDto> recentPersonas
) {
    public static UserStatsResponse of(UserService.UserStatsResult result) {
        MostChattedPersonaDto mostChatted = result.mostChattedPersona() == null ? null
                : new MostChattedPersonaDto(
                        result.mostChattedPersona().personaId(),
                        result.mostChattedPersona().name(),
                        result.mostChattedPersona().messageCount());

        List<RecentPersonaDto> recent = result.recentPersonas().stream()
                .map(r -> new RecentPersonaDto(r.personaId(), r.name(), r.lastChatAt()))
                .toList();

        return new UserStatsResponse(result.totalMessages(), result.totalConversations(),
                mostChatted, recent);
    }

    public record MostChattedPersonaDto(Long personaId, String name, Long messageCount) {}

    public record RecentPersonaDto(Long personaId, String name, LocalDateTime lastChatAt) {}
}
