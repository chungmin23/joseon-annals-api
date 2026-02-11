package com.spring.ai.joseonannalapi.storage.client;

import com.spring.ai.joseonannalapi.common.exception.FastApiException;
import com.spring.ai.joseonannalapi.domain.chat.ChatMessage;
import com.spring.ai.joseonannalapi.domain.chat.ChatSource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class FastApiChatClient {

    private final WebClient fastApiWebClient;

    public FastApiChatClient(WebClient fastApiWebClient) {
        this.fastApiWebClient = fastApiWebClient;
    }

    public FastApiChatResponse requestChat(Long personaId, String systemPrompt, String message,
                                           String roomId, List<ChatMessage> history) {
        List<FastApiChatRequest.HistoryItem> historyItems = history.stream()
                .map(msg -> new FastApiChatRequest.HistoryItem(msg.role(), msg.content()))
                .toList();

        String safeSystemPrompt = systemPrompt != null ? systemPrompt : "";

        FastApiChatRequest request = new FastApiChatRequest(
                roomId, personaId, safeSystemPrompt, message, historyItems,
                null, null, null, null, null, null);

        try {
            FastApiChatResponse response = fastApiWebClient.post()
                    .uri("/api/chat")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FastApiChatResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response == null) {
                throw new FastApiException("FastAPI 응답이 비어 있습니다.");
            }
            return response;
        } catch (FastApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw new FastApiException("FastAPI 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new FastApiException("FastAPI 연결 실패: " + e.getMessage(), e);
        }
    }

    public List<ChatSource> extractSources(FastApiChatResponse response) {
        if (response.sources() == null) {
            return Collections.emptyList();
        }
        return response.sources().stream()
                .map(s -> new ChatSource(s.documentId(), s.content(), s.similarity(),
                        s.keywordScore(), s.hybridScore()))
                .toList();
    }
}
