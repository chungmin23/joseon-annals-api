package com.spring.ai.joseonannalapi.api.controller.v1.dto.chat;

import com.spring.ai.joseonannalapi.domain.chat.ChatRoom;
import com.spring.ai.joseonannalapi.domain.persona.Persona;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long roomId,
        Long personaId,
        String personaName,
        String personaImage,
        String title,
        String greeting,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt
) {
    public static ChatRoomResponse of(ChatRoom room, Persona persona) {
        return new ChatRoomResponse(
                room.roomId(),
                room.personaId(),
                persona.name(),
                persona.profileImage(),
                room.title(),
                persona.greeting(),
                room.createdAt(),
                room.lastMessageAt()
        );
    }
}
