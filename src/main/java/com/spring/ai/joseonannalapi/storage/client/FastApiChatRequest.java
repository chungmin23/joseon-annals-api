package com.spring.ai.joseonannalapi.storage.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FastApiChatRequest(
        @JsonProperty("room_id") String roomId,
        @JsonProperty("persona_id") Long personaId,
        @JsonProperty("persona_system_prompt") String personaSystemPrompt,
        @JsonProperty("message") String message,
        @JsonProperty("history") List<HistoryItem> history
) {
    public record HistoryItem(
            @JsonProperty("role") String role,
            @JsonProperty("content") String content
    ) {
    }
}
