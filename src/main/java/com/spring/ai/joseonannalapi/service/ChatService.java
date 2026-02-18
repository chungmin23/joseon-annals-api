package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.common.exception.DailyLimitExceededException;
import com.spring.ai.joseonannalapi.domain.chat.*;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.stats.ChatStatsManager;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatClient;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatResponse;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatRoomFinder chatRoomFinder;
    private final ChatRoomManager chatRoomManager;
    private final PersonaFinder personaFinder;
    private final MessageHandler messageHandler;
    private final FastApiChatClient fastApiChatClient;
    private final ChatStatsManager chatStatsManager;
    private final ContentRecommendationService contentRecommendationService;
    private final UserFinder userFinder;

    public ChatService(ChatRoomFinder chatRoomFinder, ChatRoomManager chatRoomManager,
                       PersonaFinder personaFinder, MessageHandler messageHandler,
                       FastApiChatClient fastApiChatClient, ChatStatsManager chatStatsManager,
                       ContentRecommendationService contentRecommendationService,
                       UserFinder userFinder) {
        this.chatRoomFinder = chatRoomFinder;
        this.chatRoomManager = chatRoomManager;
        this.personaFinder = personaFinder;
        this.messageHandler = messageHandler;
        this.fastApiChatClient = fastApiChatClient;
        this.chatStatsManager = chatStatsManager;
        this.contentRecommendationService = contentRecommendationService;
        this.userFinder = userFinder;
    }

    public ChatRoom createRoom(Long userId, Long personaId) {
        Persona persona = personaFinder.getById(personaId);

        ChatRoom existing = chatRoomFinder.findByUserIdAndPersonaId(userId, personaId);
        if (existing != null) {
            return existing;
        }

        ChatRoom room = chatRoomManager.create(userId, personaId, persona.name());

        String greetingText = persona.greeting() != null ? persona.greeting() : "안녕하세요.";
        messageHandler.saveAssistantMessage(room.roomId(), persona.personaId(), greetingText, List.of());
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

    public ChatMessage sendMessage(Long roomId, Long userId, String message) {
        UserEntity user = userFinder.getEntityById(userId);
        int dailyLimit = user.getDailyLimit();
        long todayCount = chatStatsManager.getTodayCount(userId);
        if (todayCount >= dailyLimit) {
            throw new DailyLimitExceededException("오늘의 대화 횟수(" + dailyLimit + "회)를 모두 사용하였습니다. 내일 다시 대화하세요.");
        }

        ChatRoom chatRoom = chatRoomFinder.getByIdAndUserId(roomId, userId);
        Persona persona = personaFinder.getById(chatRoom.personaId());

        messageHandler.saveUserMessage(roomId, userId, message);

        List<ChatMessage> history = messageHandler.getRecentHistory(roomId, 10);

        FastApiChatResponse fastApiResponse = fastApiChatClient.requestChat(
                persona.personaId(), buildSystemPrompt(persona), message, String.valueOf(roomId), history);

        List<ChatSource> sources = fastApiChatClient.extractSources(fastApiResponse);

        ChatMessage assistantMessage = messageHandler.saveAssistantMessage(
                roomId, persona.personaId(), fastApiResponse.content(), sources);

        chatRoomManager.updateLastMessageAt(roomId);
        chatStatsManager.increment(userId, persona.personaId());

        contentRecommendationService.updateRecommendations(roomId, fastApiResponse.keywords());

        return assistantMessage;
    }
}
