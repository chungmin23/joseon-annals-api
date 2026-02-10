package com.spring.ai.joseonannalapi.api.controller.v1.dto.content;

import com.spring.ai.joseonannalapi.domain.content.RecommendedContent;

import java.util.Set;

public record ContentItemResponse(
        Long contentId,
        String title,
        String description,
        String thumbnailUrl,
        String externalUrl,
        boolean isSaved
) {
    public static ContentItemResponse of(RecommendedContent content, Set<Long> savedIds) {
        return new ContentItemResponse(
                content.contentId(),
                content.title(),
                content.description(),
                content.thumbnailUrl(),
                content.externalUrl(),
                savedIds.contains(content.contentId())
        );
    }
}
