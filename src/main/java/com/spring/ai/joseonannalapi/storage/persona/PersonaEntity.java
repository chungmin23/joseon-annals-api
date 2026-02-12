package com.spring.ai.joseonannalapi.storage.persona;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "personas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long personaId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "era", nullable = false, length = 50)
    private String reignPeriod;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "death_year")
    private Integer deathYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "personality", columnDefinition = "TEXT")
    private String personality;

    @Column(name = "speaking_style", columnDefinition = "TEXT")
    private String speakingStyle;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "greeting", columnDefinition = "TEXT")
    private String greeting;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @Column(name = "popularity_score")
    private Integer popularityScore;

    @Column(name = "total_chat_count")
    private Integer totalChatCount;

    @Column(name = "created_at", nullable = false, updatable = false)
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
}
