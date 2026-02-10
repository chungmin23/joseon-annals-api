package com.spring.ai.joseonannalapi.storage.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChatStatsRepository extends JpaRepository<UserChatStatsEntity, Long> {

    Optional<UserChatStatsEntity> findByUserIdAndPersonaId(Long userId, Long personaId);

    List<UserChatStatsEntity> findByUserId(Long userId);
}
