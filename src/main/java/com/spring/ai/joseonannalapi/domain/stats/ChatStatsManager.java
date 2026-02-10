package com.spring.ai.joseonannalapi.domain.stats;

import com.spring.ai.joseonannalapi.storage.stats.UserChatStatsEntity;
import com.spring.ai.joseonannalapi.storage.stats.UserChatStatsRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ChatStatsManager {

    private final UserChatStatsRepository userChatStatsRepository;

    public ChatStatsManager(UserChatStatsRepository userChatStatsRepository) {
        this.userChatStatsRepository = userChatStatsRepository;
    }

    @Transactional
    public void increment(Long userId, Long personaId) {
        userChatStatsRepository.findByUserIdAndPersonaId(userId, personaId)
                .ifPresentOrElse(
                        entity -> entity.increment(),
                        () -> userChatStatsRepository.save(UserChatStatsEntity.create(userId, personaId))
                );
    }

    public List<UserChatStats> getByUserId(Long userId) {
        return userChatStatsRepository.findByUserId(userId).stream()
                .map(UserChatStats::from)
                .toList();
    }
}
