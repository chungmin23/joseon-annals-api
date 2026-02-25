package com.spring.ai.joseonannalapi.storage.client;

import com.spring.ai.joseonannalapi.common.exception.FastApiException;
import com.spring.ai.joseonannalapi.domain.chat.ChatMessage;
import com.spring.ai.joseonannalapi.domain.chat.ChatSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class FastApiChatClient {

    private static final Logger log = LoggerFactory.getLogger(FastApiChatClient.class);

    private final WebClient fastApiWebClient;
    private final WebClient fastApiStreamWebClient;

    public FastApiChatClient(WebClient fastApiWebClient,
                             @Qualifier("fastApiStreamWebClient") WebClient fastApiStreamWebClient) {
        this.fastApiWebClient = fastApiWebClient;
        this.fastApiStreamWebClient = fastApiStreamWebClient;
    }

    public FastApiChatResponse requestChat(Long personaId, String systemPrompt, String message,
                                           String roomId, List<ChatMessage> history,
                                           List<String> keywords) {
        List<FastApiChatRequest.HistoryItem> historyItems = history.stream()
                .map(msg -> new FastApiChatRequest.HistoryItem(msg.role(), msg.content()))
                .toList();

        String safeSystemPrompt = systemPrompt != null ? systemPrompt : "";

        FastApiChatRequest request = new FastApiChatRequest(
                roomId, personaId, safeSystemPrompt, message, historyItems,
                null, null, null, keywords, null, null);

        log.info("[TIMING] FastAPI 호출 시작 personaId={} roomId={} historyLen={} keywords={}",
                personaId, roomId, historyItems.size(), keywords);
        long t0 = System.currentTimeMillis();

        try {
            FastApiChatResponse response = fastApiWebClient.post()
                    .uri("/api/chat")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FastApiChatResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            long elapsed = System.currentTimeMillis() - t0;
            if (response == null) {
                throw new FastApiException("FastAPI 응답이 비어 있습니다.");
            }
            log.info("[TIMING] FastAPI 응답 완료: {}ms | 응답길이={}자 | sources={}건 | keywords={}",
                    elapsed,
                    response.content() != null ? response.content().length() : 0,
                    response.sources() != null ? response.sources().size() : 0,
                    response.keywords());
            return response;
        } catch (FastApiException e) {
            log.error("[TIMING] FastAPI 예외: {}ms 경과 후 실패 - {}", System.currentTimeMillis() - t0, e.getMessage());
            throw e;
        } catch (WebClientResponseException e) {
            log.error("[TIMING] FastAPI HTTP 오류: {}ms - {} {}", System.currentTimeMillis() - t0,
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new FastApiException("FastAPI 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("[TIMING] FastAPI 연결 실패: {}ms - {}", System.currentTimeMillis() - t0, e.getMessage());
            throw new FastApiException("FastAPI 연결 실패: " + e.getMessage(), e);
        }
    }

    public Flux<String> streamRawChat(Long personaId, String systemPrompt, String message,
                                      String roomId, List<ChatMessage> history, List<String> keywords) {
        List<FastApiChatRequest.HistoryItem> historyItems = history.stream()
                .map(msg -> new FastApiChatRequest.HistoryItem(msg.role(), msg.content()))
                .toList();

        FastApiChatRequest request = new FastApiChatRequest(
                roomId, personaId, systemPrompt != null ? systemPrompt : "", message, historyItems,
                null, null, null, keywords, null, null);

        return fastApiStreamWebClient.post()
                .uri("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .mapNotNull(ServerSentEvent::data)
                .filter(data -> !data.isBlank());
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
