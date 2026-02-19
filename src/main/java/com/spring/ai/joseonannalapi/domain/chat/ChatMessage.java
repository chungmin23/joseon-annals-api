package com.spring.ai.joseonannalapi.domain.chat;

import java.util.List;

public record ChatMessage(
        String messageId,
        String role,
        String content,
        List<ChatSource> sources,
        long timestamp,
        List<String> keywords
) {
}
