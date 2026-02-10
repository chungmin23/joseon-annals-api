package com.spring.ai.joseonannalapi.domain.chat;

import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.chat.ChatRoomEntity;
import com.spring.ai.joseonannalapi.storage.chat.ChatRoomRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ChatRoomManager {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomManager(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional
    public ChatRoom create(Long userId, Long personaId, String personaName) {
        String title = personaName + "과의 대화";
        ChatRoomEntity entity = ChatRoomEntity.create(userId, personaId, title);
        ChatRoomEntity saved = chatRoomRepository.save(entity);
        return ChatRoom.from(saved);
    }

    @Transactional
    public void updateLastMessageAt(Long roomId) {
        ChatRoomEntity entity = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다. roomId=" + roomId));
        entity.updateLastMessageAt(LocalDateTime.now());
    }
}
