package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.chat.*;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.stats.ChatStatsManager;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatClient;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatResponse;
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

    public ChatService(ChatRoomFinder chatRoomFinder, ChatRoomManager chatRoomManager,
                       PersonaFinder personaFinder, MessageHandler messageHandler,
                       FastApiChatClient fastApiChatClient, ChatStatsManager chatStatsManager) {
        this.chatRoomFinder = chatRoomFinder;
        this.chatRoomManager = chatRoomManager;
        this.personaFinder = personaFinder;
        this.messageHandler = messageHandler;
        this.fastApiChatClient = fastApiChatClient;
        this.chatStatsManager = chatStatsManager;
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

    public ChatMessage sendMessage(Long roomId, Long userId, String message) {
        ChatRoom chatRoom = chatRoomFinder.getByIdAndUserId(roomId, userId);
        Persona persona = personaFinder.getById(chatRoom.personaId());

        messageHandler.saveUserMessage(roomId, userId, message);

        List<ChatMessage> history = messageHandler.getRecentHistory(roomId, 10);

        FastApiChatResponse fastApiResponse = fastApiChatClient.requestChat(
                persona.personaId(), persona.systemPrompt(), message, String.valueOf(roomId), history);

        List<ChatSource> sources = fastApiChatClient.extractSources(fastApiResponse);

        ChatMessage assistantMessage = messageHandler.saveAssistantMessage(
                roomId, persona.personaId(), fastApiResponse.content(), sources);

        chatRoomManager.updateLastMessageAt(roomId);
        chatStatsManager.increment(userId, persona.personaId());

        return assistantMessage;
    }
}
