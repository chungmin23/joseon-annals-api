package com.spring.ai.joseonannalapi.storage.persona;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "personas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "persona_id")
    private Long personaId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "reign_period", nullable = false, length = 50)
    private String reignPeriod;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
