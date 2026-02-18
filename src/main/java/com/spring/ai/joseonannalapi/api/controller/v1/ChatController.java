package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.chat.*;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.chat.ChatMessage;
import com.spring.ai.joseonannalapi.domain.chat.ChatRoom;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;
    private final PersonaFinder personaFinder;

    public ChatController(ChatService chatService, PersonaFinder personaFinder) {
        this.chatService = chatService;
        this.personaFinder = personaFinder;
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChatRoomResponse> createRoom(@LoginUser User user,
                                                    @Valid @RequestBody CreateRoomRequest request) {
        ChatRoom room = chatService.createRoom(user.userId(), request.personaId());
        Persona persona = personaFinder.getById(room.personaId());
        return ApiResponse.success(ChatRoomResponse.of(room, persona));
    }

    @GetMapping("/rooms/{roomId}")
    public ApiResponse<ChatRoomResponse> getRoom(@LoginUser User user,
                                                  @PathVariable Long roomId) {
        ChatRoom room = chatService.getRoom(roomId, user.userId());
        Persona persona = personaFinder.getById(room.personaId());
        return ApiResponse.success(ChatRoomResponse.of(room, persona));
    }

    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponse>> getRooms(@LoginUser User user) {
        List<ChatRoom> rooms = chatService.getRooms(user.userId());
        List<ChatRoomResponse> responses = rooms.stream()
                .map(room -> ChatRoomResponse.of(room, personaFinder.getById(room.personaId())))
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(@LoginUser User user,
                                                               @PathVariable Long roomId,
                                                               @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessage> messages = chatService.getMessages(roomId, user.userId(), limit);
        List<ChatMessageResponse> responses = messages.stream()
                .map(ChatMessageResponse::of)
                .toList();
        return ApiResponse.success(responses);
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessageResponse> sendMessage(@LoginUser User user,
                                                         @PathVariable Long roomId,
                                                         @Valid @RequestBody SendMessageRequest request) {
        ChatMessage message = chatService.sendMessage(roomId, user.userId(), request.message());
        return ApiResponse.success(ChatMessageResponse.of(message));
    }

    @GetMapping("/daily-usage")
    public ApiResponse<DailyUsageResponse> getDailyUsage(@LoginUser User user) {
        long usedCount = chatService.getTodayUsageCount(user.userId());
        long limitCount = chatService.getDailyLimit(user.userId());
        return ApiResponse.success(new DailyUsageResponse(usedCount, limitCount, Math.max(0, limitCount - usedCount)));
    }
}
