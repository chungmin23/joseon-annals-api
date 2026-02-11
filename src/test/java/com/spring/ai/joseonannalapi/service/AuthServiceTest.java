package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.common.exception.BusinessException;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import com.spring.ai.joseonannalapi.domain.user.UserManager;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserManager userManager;

    @Mock
    private UserFinder userFinder;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_성공() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String nickname = "테스터";
        User expected = new User(1L, email, nickname, null, LocalDateTime.now());

        given(userManager.register(email, password, nickname)).willReturn(expected);

        // when
        User result = authService.signup(email, password, nickname);

        // then
        assertThat(result).isEqualTo(expected);
        then(userManager).should().register(email, password, nickname);
    }

    @Test
    void login_성공() {
        // given
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedHash";

        UserEntity mockEntity = mock(UserEntity.class);
        given(mockEntity.getUserId()).willReturn(1L);
        given(mockEntity.getEmail()).willReturn(email);
        given(mockEntity.getNickname()).willReturn("테스터");
        given(mockEntity.getProfileImage()).willReturn(null);
        given(mockEntity.getCreatedAt()).willReturn(LocalDateTime.now());
        given(mockEntity.getPassword()).willReturn(encodedPassword);

        given(userFinder.getEntityByEmail(email)).willReturn(mockEntity);
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

        // when
        User result = authService.login(email, rawPassword);

        // then
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.userId()).isEqualTo(1L);
    }

    @Test
    void login_비밀번호_불일치_예외() {
        // given
        String email = "test@example.com";
        String rawPassword = "wrongPassword";
        String encodedPassword = "$2a$10$encodedHash";

        UserEntity mockEntity = mock(UserEntity.class);
        given(mockEntity.getPassword()).willReturn(encodedPassword);
        given(userFinder.getEntityByEmail(email)).willReturn(mockEntity);
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(email, rawPassword))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
