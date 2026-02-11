package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.AuthResponse;
import com.spring.ai.joseonannalapi.common.exception.BusinessException;
import com.spring.ai.joseonannalapi.config.JwtTokenProvider;
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

    public AuthService(UserManager userManager, UserFinder userFinder,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userManager = userManager;
        this.userFinder = userFinder;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse signup(String email, String rawPassword, String nickname) {
        User user = userManager.register(email, rawPassword, nickname);
        String token = jwtTokenProvider.generateToken(user.userId());
        return AuthResponse.of(user, token);
    }

    public AuthResponse login(String email, String rawPassword) {
        UserEntity entity = userFinder.getEntityByEmail(email);
        if (!passwordEncoder.matches(rawPassword, entity.getPassword())) {
            throw new BusinessException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        User user = User.from(entity);
        String token = jwtTokenProvider.generateToken(user.userId());
        return AuthResponse.of(user, token);
    }
}
