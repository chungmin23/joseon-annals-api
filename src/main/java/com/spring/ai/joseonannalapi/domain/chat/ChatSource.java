package com.spring.ai.joseonannalapi.domain.chat;

public record ChatSource(
        int documentId,
        String content,
        double similarity,
        double keywordScore,
        double hybridScore
) {
}
