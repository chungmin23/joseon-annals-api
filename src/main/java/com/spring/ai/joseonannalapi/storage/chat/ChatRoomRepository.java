package com.spring.ai.joseonannalapi.storage.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    List<ChatRoomEntity> findByUserIdOrderByLastMessageAtDesc(Long userId);

    Optional<ChatRoomEntity> findByRoomIdAndUserId(Long roomId, Long userId);
}
