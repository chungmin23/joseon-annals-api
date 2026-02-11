package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.interest.InterestManager;
import com.spring.ai.joseonannalapi.domain.interest.UserInterest;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.stats.ChatStatsManager;
import com.spring.ai.joseonannalapi.domain.stats.UserChatStats;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
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
class UserServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private InterestManager interestManager;

    @Mock
    private ChatStatsManager chatStatsManager;

    @Mock
    private PersonaFinder personaFinder;

    @InjectMocks
    private UserService userService;

    private User sampleUser(Long userId) {
        return new User(userId, "test@example.com", "테스터", null, LocalDateTime.now());
    }

    private Persona samplePersona(Long id, String name) {
        return new Persona(id, name, name + "왕", "1400-1450",
                "설명", null, "프롬프트", new String[]{"역사"});
    }

    @Test
    void getMe_성공() {
        // given
        Long userId = 1L;
        User expected = sampleUser(userId);
        given(userFinder.getById(userId)).willReturn(expected);

        // when
        User result = userService.getMe(userId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void updateInterests_성공() {
        // given
        Long userId = 1L;
        List<UserInterest> interests = List.of(
                new UserInterest(userId, "era", "조선 초기", 1.0),
                new UserInterest(userId, "field", "정치", 0.8)
        );
        given(interestManager.replaceAll(userId, interests)).willReturn(List.of());

        // when
        userService.updateInterests(userId, interests);

        // then
        then(interestManager).should().replaceAll(userId, interests);
    }

    @Test
    void getStats_통계_없을때_기본값_반환() {
        // given
        Long userId = 1L;
        given(chatStatsManager.getByUserId(userId)).willReturn(List.of());

        // when
        UserService.UserStatsResult result = userService.getStats(userId);

        // then
        assertThat(result.totalMessages()).isEqualTo(0L);
        assertThat(result.totalConversations()).isEqualTo(0L);
        assertThat(result.mostChattedPersona()).isNull();
        assertThat(result.recentPersonas()).isEmpty();
    }

    @Test
    void getStats_통계_있을때_올바르게_집계() {
        // given
        Long userId = 1L;
        LocalDateTime older = LocalDateTime.now().minusDays(2);
        LocalDateTime newer = LocalDateTime.now().minusDays(1);

        List<UserChatStats> statsList = List.of(
                new UserChatStats(userId, 1L, 50L, newer),
                new UserChatStats(userId, 2L, 30L, older)
        );

        given(chatStatsManager.getByUserId(userId)).willReturn(statsList);
        given(personaFinder.getById(1L)).willReturn(samplePersona(1L, "세종"));
        given(personaFinder.getById(2L)).willReturn(samplePersona(2L, "태종"));

        // when
        UserService.UserStatsResult result = userService.getStats(userId);

        // then
        assertThat(result.totalMessages()).isEqualTo(80L);
        assertThat(result.totalConversations()).isEqualTo(2L);

        assertThat(result.mostChattedPersona()).isNotNull();
        assertThat(result.mostChattedPersona().personaId()).isEqualTo(1L);
        assertThat(result.mostChattedPersona().name()).isEqualTo("세종");
        assertThat(result.mostChattedPersona().messageCount()).isEqualTo(50L);

        assertThat(result.recentPersonas()).hasSize(2);
        assertThat(result.recentPersonas().get(0).personaId()).isEqualTo(1L);
        assertThat(result.recentPersonas().get(1).personaId()).isEqualTo(2L);
    }

    @Test
    void getStats_최근_페르소나_5개_제한() {
        // given
        Long userId = 1L;
        LocalDateTime base = LocalDateTime.now();

        List<UserChatStats> statsList = List.of(
                new UserChatStats(userId, 1L, 10L, base.minusDays(1)),
                new UserChatStats(userId, 2L, 20L, base.minusDays(2)),
                new UserChatStats(userId, 3L, 30L, base.minusDays(3)),
                new UserChatStats(userId, 4L, 40L, base.minusDays(4)),
                new UserChatStats(userId, 5L, 50L, base.minusDays(5)),
                new UserChatStats(userId, 6L, 60L, base.minusDays(6))
        );

        given(chatStatsManager.getByUserId(userId)).willReturn(statsList);
        for (long i = 1; i <= 6; i++) {
            given(personaFinder.getById(i)).willReturn(samplePersona(i, "페르소나" + i));
        }

        // when
        UserService.UserStatsResult result = userService.getStats(userId);

        // then
        assertThat(result.recentPersonas()).hasSize(5);
        assertThat(result.recentPersonas().get(0).personaId()).isEqualTo(1L);
    }
}
