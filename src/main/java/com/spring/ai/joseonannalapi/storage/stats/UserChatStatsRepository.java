package com.spring.ai.joseonannalapi.storage.stats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserChatStatsRepository extends JpaRepository<UserChatStatsEntity, Long> {

    Optional<UserChatStatsEntity> findByUserIdAndStatDate(Long userId, LocalDate statDate);

    List<UserChatStatsEntity> findByUserId(Long userId);
}
