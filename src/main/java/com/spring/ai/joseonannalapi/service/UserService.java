package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.interest.InterestManager;
import com.spring.ai.joseonannalapi.domain.interest.UserInterest;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.stats.ChatStatsManager;
import com.spring.ai.joseonannalapi.domain.stats.UserChatStats;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private final UserFinder userFinder;
    private final InterestManager interestManager;
    private final ChatStatsManager chatStatsManager;
    private final PersonaFinder personaFinder;

    public UserService(UserFinder userFinder, InterestManager interestManager,
                       ChatStatsManager chatStatsManager, PersonaFinder personaFinder) {
        this.userFinder = userFinder;
        this.interestManager = interestManager;
        this.chatStatsManager = chatStatsManager;
        this.personaFinder = personaFinder;
    }

    public User getMe(Long userId) {
        return userFinder.getById(userId);
    }

    public void updateInterests(Long userId, List<UserInterest> interests) {
        interestManager.replaceAll(userId, interests);
    }

    public UserStatsResult getStats(Long userId) {
        List<UserChatStats> statsList = chatStatsManager.getByUserId(userId);

        long totalMessages = statsList.stream().mapToLong(UserChatStats::messageCount).sum();
        long totalConversations = statsList.size();

        UserChatStats mostChattedStats = statsList.stream()
                .max(Comparator.comparingLong(UserChatStats::messageCount))
                .orElse(null);

        MostChattedPersona mostChattedPersona = null;
        if (mostChattedStats != null) {
            String personaName = personaFinder.getById(mostChattedStats.personaId()).name();
            mostChattedPersona = new MostChattedPersona(
                    mostChattedStats.personaId(), personaName, mostChattedStats.messageCount());
        }

        List<RecentPersona> recentPersonas = statsList.stream()
                .sorted(Comparator.comparing(UserChatStats::lastChatAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(s -> new RecentPersona(s.personaId(),
                        personaFinder.getById(s.personaId()).name(), s.lastChatAt()))
                .toList();

        return new UserStatsResult(totalMessages, totalConversations, mostChattedPersona, recentPersonas);
    }

    public record MostChattedPersona(Long personaId, String name, Long messageCount) {}

    public record RecentPersona(Long personaId, String name, java.time.LocalDateTime lastChatAt) {}

    public record UserStatsResult(
            Long totalMessages,
            Long totalConversations,
            MostChattedPersona mostChattedPersona,
            List<RecentPersona> recentPersonas
    ) {}
}
