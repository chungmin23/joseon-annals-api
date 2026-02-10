package com.spring.ai.joseonannalapi.storage.content;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_libraries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLibraryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_id")
    private Long libraryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }

    public static UserLibraryEntity create(Long userId, Long contentId) {
        UserLibraryEntity entity = new UserLibraryEntity();
        entity.userId = userId;
        entity.contentId = contentId;
        return entity;
    }
}
