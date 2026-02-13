package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.AuthResponse;
import com.spring.ai.joseonannalapi.common.exception.BusinessException;
import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.config.JwtTokenProvider;
import com.spring.ai.joseonannalapi.domain.auth.RefreshTokenManager;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import com.spring.ai.joseonannalapi.domain.user.UserManager;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserManager userManager;
    private final UserFinder userFinder;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenManager refreshTokenManager;

    public AuthService(UserManager userManager, UserFinder userFinder,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenManager refreshTokenManager) {
        this.userManager = userManager;
        this.userFinder = userFinder;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenManager = refreshTokenManager;
    }

    public AuthResponse signup(String email, String rawPassword, String nickname) {
        User user = userManager.register(email, rawPassword, nickname);
        String accessToken = jwtTokenProvider.generateToken(user.userId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.userId());
        refreshTokenManager.save(user.userId(), refreshToken);
        return AuthResponse.of(user, accessToken, refreshToken);
    }

    public AuthResponse login(String email, String rawPassword) {
        UserEntity entity;
        try {
            entity = userFinder.getEntityByEmail(email);
        } catch (NotFoundException e) {
            throw new com.spring.ai.joseonannalapi.common.exception.InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!passwordEncoder.matches(rawPassword, entity.getPassword())) {
            throw new com.spring.ai.joseonannalapi.common.exception.InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        User user = User.from(entity);
        String accessToken = jwtTokenProvider.generateToken(user.userId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.userId());
        refreshTokenManager.save(user.userId(), refreshToken);
        return AuthResponse.of(user, accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshToken) {
        Long userId = refreshTokenManager.validateAndGetUserId(refreshToken);
        User user = userFinder.getById(userId);
        String newAccessToken = jwtTokenProvider.generateToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
        refreshTokenManager.save(userId, newRefreshToken);
        return AuthResponse.of(user, newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenManager.deleteByToken(refreshToken);
    }
}
