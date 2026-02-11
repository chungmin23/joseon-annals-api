package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.chat.*;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.stats.ChatStatsManager;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatClient;
import com.spring.ai.joseonannalapi.storage.client.FastApiChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomFinder chatRoomFinder;

    @Mock
    private ChatRoomManager chatRoomManager;

    @Mock
    private PersonaFinder personaFinder;

    @Mock
    private MessageHandler messageHandler;

    @Mock
    private FastApiChatClient fastApiChatClient;

    @Mock
    private ChatStatsManager chatStatsManager;

    @InjectMocks
    private ChatService chatService;

    private Persona samplePersona() {
        return new Persona(1L, "세종", "세종대왕", "1418-1450",
                "훈민정음 창제", null, "당신은 세종대왕입니다.", new String[]{"언어", "과학"});
    }

    private ChatRoom sampleRoom(Long roomId, Long userId, Long personaId) {
        return new ChatRoom(roomId, userId, personaId, "세종과의 대화",
                LocalDateTime.now(), null);
    }

    @Test
    void createRoom_성공() {
        // given
        Long userId = 1L;
        Long personaId = 1L;
        Persona persona = samplePersona();
        ChatRoom expected = sampleRoom(10L, userId, personaId);

        given(personaFinder.getById(personaId)).willReturn(persona);
        given(chatRoomManager.create(userId, personaId, persona.name())).willReturn(expected);

        // when
        ChatRoom result = chatService.createRoom(userId, personaId);

        // then
        assertThat(result.roomId()).isEqualTo(10L);
        assertThat(result.userId()).isEqualTo(userId);
        then(personaFinder).should().getById(personaId);
        then(chatRoomManager).should().create(userId, personaId, persona.name());
    }

    @Test
    void getRooms_성공() {
        // given
        Long userId = 1L;
        List<ChatRoom> expected = List.of(
                sampleRoom(1L, userId, 1L),
                sampleRoom(2L, userId, 2L)
        );

        given(chatRoomFinder.getByUserId(userId)).willReturn(expected);

        // when
        List<ChatRoom> result = chatService.getRooms(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getMessages_성공() {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        int limit = 20;
        ChatRoom room = sampleRoom(roomId, userId, 1L);
        List<ChatMessage> messages = List.of(
                new ChatMessage("msg-1", "user", "안녕하세요", List.of(), 1000L),
                new ChatMessage("msg-2", "assistant", "반갑소이다", List.of(), 2000L)
        );

        given(chatRoomFinder.getByIdAndUserId(roomId, userId)).willReturn(room);
        given(messageHandler.getRecentHistory(roomId, limit)).willReturn(messages);

        // when
        List<ChatMessage> result = chatService.getMessages(roomId, userId, limit);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).role()).isEqualTo("user");
        assertThat(result.get(1).role()).isEqualTo("assistant");
    }

    @Test
    void sendMessage_성공() {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        Long personaId = 1L;
        String userMessage = "한글 창제에 대해 알려주세요";

        ChatRoom room = sampleRoom(roomId, userId, personaId);
        Persona persona = samplePersona();
        List<ChatMessage> history = List.of(
                new ChatMessage("msg-1", "user", "안녕하세요", List.of(), 1000L)
        );
        FastApiChatResponse fastApiResponse = new FastApiChatResponse("훈민정음은...", List.of());
        ChatMessage assistantMessage = new ChatMessage("msg-2", "assistant", "훈민정음은...", List.of(), 2000L);

        given(chatRoomFinder.getByIdAndUserId(roomId, userId)).willReturn(room);
        given(personaFinder.getById(personaId)).willReturn(persona);
        given(messageHandler.getRecentHistory(roomId, 10)).willReturn(history);
        given(fastApiChatClient.requestChat(
                persona.personaId(), persona.systemPrompt(), userMessage,
                String.valueOf(roomId), history)).willReturn(fastApiResponse);
        given(fastApiChatClient.extractSources(fastApiResponse)).willReturn(List.of());
        given(messageHandler.saveAssistantMessage(roomId, personaId, "훈민정음은...", List.of()))
                .willReturn(assistantMessage);

        // when
        ChatMessage result = chatService.sendMessage(roomId, userId, userMessage);

        // then
        assertThat(result.role()).isEqualTo("assistant");
        assertThat(result.content()).isEqualTo("훈민정음은...");
        then(messageHandler).should().saveUserMessage(roomId, userId, userMessage);
        then(chatRoomManager).should().updateLastMessageAt(roomId);
        then(chatStatsManager).should().increment(userId, personaId);
    }
}
