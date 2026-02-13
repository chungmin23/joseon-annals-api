package com.spring.ai.joseonannalapi.storage.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    List<ChatRoomEntity> findByUserIdOrderByLastMessageAtDesc(Long userId);

    Optional<ChatRoomEntity> findByRoomIdAndUserId(Long roomId, Long userId);

    Optional<ChatRoomEntity> findFirstByUserIdAndPersonaIdOrderByCreatedAtDesc(Long userId, Long personaId);

    @Query("SELECT DISTINCT cr.personaId FROM ChatRoomEntity cr WHERE cr.userId = :userId")
    List<Long> findDistinctPersonaIdsByUserId(@Param("userId") Long userId);
}
