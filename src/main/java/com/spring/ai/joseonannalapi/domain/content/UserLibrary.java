package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.storage.content.UserLibraryEntity;

import java.time.LocalDateTime;

public record UserLibrary(
        Long libraryId,
        Long userId,
        Long contentId,
        LocalDateTime savedAt
) {
    public static UserLibrary from(UserLibraryEntity entity) {
        return new UserLibrary(
                entity.getLibraryId(),
                entity.getUserId(),
                entity.getContentId(),
                entity.getSavedAt()
        );
    }
}
