package com.spring.ai.joseonannalapi.api.controller.v1.dto.chat;

import com.spring.ai.joseonannalapi.domain.chat.ChatMessage;
import com.spring.ai.joseonannalapi.domain.chat.ChatSource;

import java.util.List;

public record ChatMessageResponse(
        String messageId,
        String role,
        String content,
        List<ChatSource> sources,
        long timestamp
) {
    public static ChatMessageResponse of(ChatMessage message) {
        return new ChatMessageResponse(
                message.messageId(),
                message.role(),
                message.content(),
                message.sources(),
                message.timestamp()
        );
    }
}
