package com.spring.ai.joseonannalapi.storage.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FastApiChatResponse(
        @JsonProperty("content") String content,
        @JsonProperty("sources") List<SourceItem> sources
) {
    public record SourceItem(
            @JsonProperty("document_id") int documentId,
            @JsonProperty("content") String content,
            @JsonProperty("similarity") double similarity,
            @JsonProperty("keyword_score") double keywordScore,
            @JsonProperty("hybrid_score") double hybridScore
    ) {
    }
}
