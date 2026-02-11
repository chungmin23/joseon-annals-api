package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.storage.content.RecommendedContentEntity;

public record RecommendedContent(
        Long contentId,
        ContentType contentType,
        String title,
        String description,
        String thumbnailUrl,
        String linkUrl,
        Integer displayOrder
) {
    public static RecommendedContent from(RecommendedContentEntity entity) {
        return new RecommendedContent(
                entity.getContentId(),
                entity.getContentType(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getLinkUrl(),
                entity.getDisplayOrder()
        );
    }
}
