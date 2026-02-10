package com.spring.ai.joseonannalapi.storage.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static ChatRoomEntity create(Long userId, Long personaId, String title) {
        ChatRoomEntity entity = new ChatRoomEntity();
        entity.userId = userId;
        entity.personaId = personaId;
        entity.title = title;
        return entity;
    }

    public void updateLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
