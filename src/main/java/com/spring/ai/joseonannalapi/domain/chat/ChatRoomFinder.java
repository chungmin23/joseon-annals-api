package com.spring.ai.joseonannalapi.domain.chat;

import com.spring.ai.joseonannalapi.common.exception.ForbiddenException;
import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.chat.ChatRoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatRoomFinder {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomFinder(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    public ChatRoom getById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .map(ChatRoom::from)
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다. roomId=" + roomId));
    }

    public List<ChatRoom> getByUserId(Long userId) {
        return chatRoomRepository.findByUserIdOrderByLastMessageAtDesc(userId).stream()
                .map(ChatRoom::from)
                .toList();
    }

    public ChatRoom getByIdAndUserId(Long roomId, Long userId) {
        return chatRoomRepository.findByRoomIdAndUserId(roomId, userId)
                .map(ChatRoom::from)
                .orElseThrow(() -> new ForbiddenException("해당 채팅방에 접근 권한이 없습니다."));
    }

    public ChatRoom findByUserIdAndPersonaId(Long userId, Long personaId) {
        return chatRoomRepository.findFirstByUserIdAndPersonaIdOrderByCreatedAtDesc(userId, personaId)
                .map(ChatRoom::from)
                .orElse(null);
    }
}
