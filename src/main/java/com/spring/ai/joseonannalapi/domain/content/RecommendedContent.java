package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.storage.content.RecommendedContentEntity;

public record RecommendedContent(
        Long contentId,
        Long personaId,
        ContentType contentType,
        String title,
        String description,
        String thumbnailUrl,
        String externalUrl,
        Integer displayOrder
) {
    public static RecommendedContent from(RecommendedContentEntity entity) {
        return new RecommendedContent(
                entity.getContentId(),
                entity.getPersonaId(),
                entity.getContentType(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getExternalUrl(),
                entity.getDisplayOrder()
        );
    }
}
