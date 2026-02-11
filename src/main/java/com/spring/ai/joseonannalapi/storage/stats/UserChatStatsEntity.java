package com.spring.ai.joseonannalapi.storage.stats;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "persona_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChatStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long statsId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @Column(name = "message_count", nullable = false)
    private Long messageCount;

    @Column(name = "last_chat_at")
    private LocalDateTime lastChatAt;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    public static UserChatStatsEntity create(Long userId, Long personaId) {
        UserChatStatsEntity entity = new UserChatStatsEntity();
        entity.userId = userId;
        entity.personaId = personaId;
        entity.messageCount = 1L;
        entity.lastChatAt = LocalDateTime.now();
        entity.statDate = LocalDate.now();
        return entity;
    }

    public void increment() {
        this.messageCount++;
        this.lastChatAt = LocalDateTime.now();
        this.statDate = LocalDate.now();
    }
}
