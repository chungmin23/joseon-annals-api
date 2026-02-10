package com.spring.ai.joseonannalapi.domain.chat;

import com.spring.ai.joseonannalapi.storage.chat.ChatRoomEntity;

import java.time.LocalDateTime;

public record ChatRoom(
        Long roomId,
        Long userId,
        Long personaId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt
) {
    public static ChatRoom from(ChatRoomEntity entity) {
        return new ChatRoom(
                entity.getRoomId(),
                entity.getUserId(),
                entity.getPersonaId(),
                entity.getTitle(),
                entity.getCreatedAt(),
                entity.getLastMessageAt()
        );
    }
}
