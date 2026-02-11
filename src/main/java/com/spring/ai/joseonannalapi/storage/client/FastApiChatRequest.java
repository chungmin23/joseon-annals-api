package com.spring.ai.joseonannalapi.storage.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FastApiChatRequest(
        @JsonProperty("room_id") String roomId,
        @JsonProperty("persona_id") Long personaId,
        @JsonProperty("persona_system_prompt") String personaSystemPrompt,
        @JsonProperty("message") String message,
        @JsonProperty("history") List<HistoryItem> history,
        @JsonProperty("chunk_overlap") Integer chunkOverlap,
        @JsonProperty("similarity_cutoff") Double similarityCutoff,
        @JsonProperty("top_k") Integer topK,
        @JsonProperty("keywords") List<String> keywords,
        @JsonProperty("category") String category,
        @JsonProperty("keyword_weight") Double keywordWeight
) {
    public record HistoryItem(
            @JsonProperty("role") String role,
            @JsonProperty("content") String content
    ) {
    }
}
