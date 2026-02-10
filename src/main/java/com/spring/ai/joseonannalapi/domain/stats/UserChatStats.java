package com.spring.ai.joseonannalapi.domain.stats;

import com.spring.ai.joseonannalapi.storage.stats.UserChatStatsEntity;

import java.time.LocalDateTime;

public record UserChatStats(
        Long userId,
        Long personaId,
        Long messageCount,
        LocalDateTime lastChatAt
) {
    public static UserChatStats from(UserChatStatsEntity entity) {
        return new UserChatStats(
                entity.getUserId(),
                entity.getPersonaId(),
                entity.getMessageCount(),
                entity.getLastChatAt()
        );
    }
}
