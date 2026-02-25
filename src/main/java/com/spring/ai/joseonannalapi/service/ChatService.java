package com.spring.ai.joseonannalapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.joseonannalapi.common.exception.DailyLimitExceededException;
import com.spring.ai.joseonannalapi.domain.chat.*;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.stats.ChatStatsManager;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatClient;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatResponse;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRoomFinder chatRoomFinder;
    private final ChatRoomManager chatRoomManager;
    private final PersonaFinder personaFinder;
    private final MessageHandler messageHandler;
    private final FastApiChatClient fastApiChatClient;
    private final ChatStatsManager chatStatsManager;
    private final ContentRecommendationService contentRecommendationService;
    private final UserFinder userFinder;
    private final ObjectMapper objectMapper;

    public ChatService(ChatRoomFinder chatRoomFinder, ChatRoomManager chatRoomManager,
                       PersonaFinder personaFinder, MessageHandler messageHandler,
                       FastApiChatClient fastApiChatClient, ChatStatsManager chatStatsManager,
                       ContentRecommendationService contentRecommendationService,
                       UserFinder userFinder, ObjectMapper objectMapper) {
        this.chatRoomFinder = chatRoomFinder;
        this.chatRoomManager = chatRoomManager;
        this.personaFinder = personaFinder;
        this.messageHandler = messageHandler;
        this.fastApiChatClient = fastApiChatClient;
        this.chatStatsManager = chatStatsManager;
        this.contentRecommendationService = contentRecommendationService;
        this.userFinder = userFinder;
        this.objectMapper = objectMapper;
    }

    public ChatRoom createRoom(Long userId, Long personaId) {
        Persona persona = personaFinder.getById(personaId);

        ChatRoom existing = chatRoomFinder.findByUserIdAndPersonaId(userId, personaId);
        if (existing != null) {
            return existing;
        }

        ChatRoom room = chatRoomManager.create(userId, personaId, persona.name());

        String greetingText = persona.greeting() != null ? persona.greeting() : "안녕하세요.";
        messageHandler.saveAssistantMessage(room.roomId(), persona.personaId(), greetingText, List.of(), null);
        chatRoomManager.updateLastMessageAt(room.roomId());

        return room;
    }

    public ChatRoom getRoom(Long roomId, Long userId) {
        return chatRoomFinder.getByIdAndUserId(roomId, userId);
    }

    public List<ChatRoom> getRooms(Long userId) {
        return chatRoomFinder.getByUserId(userId);
    }

    public List<ChatMessage> getMessages(Long roomId, Long userId, int limit) {
        chatRoomFinder.getByIdAndUserId(roomId, userId);
        return messageHandler.getRecentHistory(roomId, limit);
    }

    private String buildSystemPrompt(Persona persona) {
        if (persona.systemPrompt() != null && !persona.systemPrompt().isBlank()) {
            return persona.systemPrompt();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 조선시대 ").append(persona.name()).append("입니다.\n");
        if (persona.description() != null) {
            sb.append("배경: ").append(persona.description()).append("\n");
        }
        if (persona.personality() != null) {
            sb.append("성격: ").append(persona.personality()).append("\n");
        }
        if (persona.speakingStyle() != null) {
            sb.append("말투: ").append(persona.speakingStyle()).append("\n");
        }
        sb.append("\n");
        sb.append("【중요 지침】\n");
        sb.append("- 반드시 위의 말투와 성격을 유지하며 ").append(persona.name()).append("이 직접 말하는 것처럼 대화하세요.\n");
        sb.append("- AI처럼 정보를 나열하거나 해설하지 마세요. 왕으로서 자신의 경험과 감정을 담아 이야기하세요.\n");
        sb.append("- 역사적 사실에 기반하되, 1인칭 시점으로 답하세요.\n");
        sb.append("- 질문자를 '경', '그대', '백성' 등 시대에 맞는 호칭으로 부르세요.\n");
        sb.append("- 답변은 자연스러운 대화체로, 너무 길지 않게 작성하세요.");
        return sb.toString();
    }

    public long getTodayUsageCount(Long userId) {
        return chatStatsManager.getTodayCount(userId);
    }

    public int getDailyLimit(Long userId) {
        return userFinder.getEntityById(userId).getDailyLimit();
    }

    public Flux<String> streamMessage(Long roomId, Long userId, String message) {
        return Mono.fromCallable(() -> {
            // ① 일일 한도 확인
            UserEntity user = userFinder.getEntityById(userId);
            long todayCount = chatStatsManager.getTodayCount(userId);
            if (todayCount >= user.getDailyLimit()) {
                throw new DailyLimitExceededException(
                        "오늘의 대화 횟수(" + user.getDailyLimit() + "회)를 모두 사용하였습니다. 내일 다시 대화하세요.");
            }
            // ② 채팅방·페르소나 조회
            ChatRoom chatRoom = chatRoomFinder.getByIdAndUserId(roomId, userId);
            Persona persona = personaFinder.getById(chatRoom.personaId());
            // ③ 사용자 메시지 저장
            messageHandler.saveUserMessage(roomId, userId, message);
            // ④ 히스토리 조회
            List<ChatMessage> history = messageHandler.getRecentHistory(roomId, 10);
            List<String> prevKeywords = history.stream()
                    .filter(m -> "assistant".equals(m.role()))
                    .reduce((a, b) -> b)
                    .map(ChatMessage::keywords)
                    .orElse(null);
            return new StreamPreContext(persona, history, prevKeywords);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(ctx -> {
            StringBuilder accumulated = new StringBuilder();
            return fastApiChatClient.streamRawChat(
                    ctx.persona().personaId(), buildSystemPrompt(ctx.persona()),
                    message, String.valueOf(roomId), ctx.history(), ctx.prevKeywords())
                .concatMap(data -> {
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        String type = node.get("type").asText();
                        if ("token".equals(type)) {
                            accumulated.append(node.get("content").asText());
                            return Mono.just(data);
                        } else if ("done".equals(type)) {
                            List<String> keywords = objectMapper.convertValue(
                                    node.get("keywords"), new TypeReference<>() {});
                            List<ChatSource> sources = parseSources(node.get("sources"));
                            return Mono.fromCallable(() -> {
                                ChatMessage saved = messageHandler.saveAssistantMessage(
                                        roomId, ctx.persona().personaId(),
                                        accumulated.toString(), sources, keywords);
                                chatRoomManager.updateLastMessageAt(roomId);
                                chatStatsManager.increment(userId, ctx.persona().personaId());
                                contentRecommendationService.updateRecommendations(roomId, keywords);
                                return "{\"type\":\"saved\",\"messageId\":\"" + saved.messageId() + "\"}";
                            }).subscribeOn(Schedulers.boundedElastic());
                        }
                    } catch (Exception e) {
                        log.error("[Stream] 이벤트 파싱 오류: {}", e.getMessage());
                    }
                    return Mono.empty();
                });
        });
    }

    private List<ChatSource> parseSources(JsonNode sourcesNode) {
        List<ChatSource> sources = new ArrayList<>();
        if (sourcesNode != null && sourcesNode.isArray()) {
            for (JsonNode src : sourcesNode) {
                sources.add(new ChatSource(
                        src.get("documentId").asInt(),
                        src.get("content").asText(),
                        src.get("similarity").asDouble(),
                        src.get("keywordScore").asDouble(),
                        src.get("hybridScore").asDouble()
                ));
            }
        }
        return sources;
    }

    private record StreamPreContext(Persona persona, List<ChatMessage> history, List<String> prevKeywords) {}

    public ChatMessage sendMessage(Long roomId, Long userId, String message) {
        long tTotal = System.currentTimeMillis();
        log.info("[TIMING] ===== sendMessage 시작 roomId={} userId={} =====", roomId, userId);

        // ① 일일 한도 확인
        long t0 = System.currentTimeMillis();
        UserEntity user = userFinder.getEntityById(userId);
        int dailyLimit = user.getDailyLimit();
        long todayCount = chatStatsManager.getTodayCount(userId);
        log.info("[TIMING] ① 일일한도 확인: {}ms (사용 {}/{})", System.currentTimeMillis() - t0, todayCount, dailyLimit);
        if (todayCount >= dailyLimit) {
            throw new DailyLimitExceededException("오늘의 대화 횟수(" + dailyLimit + "회)를 모두 사용하였습니다. 내일 다시 대화하세요.");
        }

        // ② 채팅방·페르소나 조회
        t0 = System.currentTimeMillis();
        ChatRoom chatRoom = chatRoomFinder.getByIdAndUserId(roomId, userId);
        Persona persona = personaFinder.getById(chatRoom.personaId());
        log.info("[TIMING] ② 채팅방·페르소나 조회: {}ms persona={}", System.currentTimeMillis() - t0, persona.name());

        // ③ 사용자 메시지 저장 (DynamoDB)
        t0 = System.currentTimeMillis();
        messageHandler.saveUserMessage(roomId, userId, message);
        log.info("[TIMING] ③ 사용자 메시지 저장(DynamoDB): {}ms", System.currentTimeMillis() - t0);

        // ④ 히스토리 조회 (DynamoDB)
        t0 = System.currentTimeMillis();
        List<ChatMessage> history = messageHandler.getRecentHistory(roomId, 10);
        List<String> previousKeywords = history.stream()
                .filter(m -> "assistant".equals(m.role()))
                .reduce((a, b) -> b)
                .map(ChatMessage::keywords)
                .orElse(null);
        log.info("[TIMING] ④ 히스토리 조회(DynamoDB): {}ms history={}건 prevKeywords={}",
                System.currentTimeMillis() - t0, history.size(), previousKeywords);

        // ⑤ FastAPI 호출 (RAG + LLM — 가장 오래 걸리는 구간)
        t0 = System.currentTimeMillis();
        FastApiChatResponse fastApiResponse = fastApiChatClient.requestChat(
                persona.personaId(), buildSystemPrompt(persona), message, String.valueOf(roomId),
                history, previousKeywords);
        log.info("[TIMING] ⑤ FastAPI 전체 (RAG+LLM+KW): {}ms", System.currentTimeMillis() - t0);

        // ⑥ 어시스턴트 메시지 저장 (DynamoDB)
        t0 = System.currentTimeMillis();
        List<ChatSource> sources = fastApiChatClient.extractSources(fastApiResponse);
        ChatMessage assistantMessage = messageHandler.saveAssistantMessage(
                roomId, persona.personaId(), fastApiResponse.content(), sources, fastApiResponse.keywords());
        log.info("[TIMING] ⑥ 어시스턴트 메시지 저장(DynamoDB): {}ms", System.currentTimeMillis() - t0);

        // ⑦ 통계·추천 업데이트 (비동기)
        t0 = System.currentTimeMillis();
        chatRoomManager.updateLastMessageAt(roomId);
        chatStatsManager.increment(userId, persona.personaId());
        contentRecommendationService.updateRecommendations(roomId, fastApiResponse.keywords());
        log.info("[TIMING] ⑦ 통계·추천 업데이트: {}ms", System.currentTimeMillis() - t0);

        log.info("[TIMING] ===== sendMessage 완료: 전체 {}ms =====", System.currentTimeMillis() - tTotal);
        return assistantMessage;
    }
}
