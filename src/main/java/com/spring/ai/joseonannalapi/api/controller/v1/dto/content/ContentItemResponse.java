package com.spring.ai.joseonannalapi.api.controller.v1.dto.content;

import com.spring.ai.joseonannalapi.domain.content.RecommendedContent;

import java.util.Set;

public record ContentItemResponse(
        Long contentId,
        String contentType,
        String title,
        String description,
        String thumbnailUrl,
        String linkUrl,
        boolean isSaved
) {
    public static ContentItemResponse of(RecommendedContent content, Set<Long> savedIds) {
        return new ContentItemResponse(
                content.contentId(),
                content.contentType().name(),
                content.title(),
                content.description(),
                content.thumbnailUrl(),
                content.linkUrl(),
                savedIds.contains(content.contentId())
        );
    }
}
