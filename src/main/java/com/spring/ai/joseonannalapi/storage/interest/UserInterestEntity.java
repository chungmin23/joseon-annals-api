package com.spring.ai.joseonannalapi.storage.interest;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long interestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "score")
    private Integer score;

    @Column(name = "chat_count")
    private Integer chatCount;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "last_updated_chat_id")
    private Long lastUpdatedChatId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static UserInterestEntity create(Long userId, String category, String value, Double weight) {
        UserInterestEntity entity = new UserInterestEntity();
        entity.userId = userId;
        entity.category = category;
        entity.value = value;
        entity.weight = weight;
        return entity;
    }

    public String getValue() {
        return value;
    }
}
