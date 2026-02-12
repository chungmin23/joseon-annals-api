package com.spring.ai.joseonannalapi.domain.auth;

import com.spring.ai.joseonannalapi.common.exception.BusinessException;
import com.spring.ai.joseonannalapi.storage.auth.RefreshTokenEntity;
import com.spring.ai.joseonannalapi.storage.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RefreshTokenManager {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpiration;

    public RefreshTokenManager(RefreshTokenRepository refreshTokenRepository,
                                @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpiration = refreshExpiration;
    }

    @Transactional
    public void save(Long userId, String token) {
        refreshTokenRepository.deleteByUserId(userId);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
        refreshTokenRepository.save(RefreshTokenEntity.create(userId, token, expiresAt));
    }

    @Transactional(readOnly = true)
    public Long validateAndGetUserId(String token) {
        RefreshTokenEntity entity = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."));
        if (entity.isExpired()) {
            refreshTokenRepository.deleteByToken(token);
            throw new BusinessException("EXPIRED_REFRESH_TOKEN", "만료된 리프레시 토큰입니다.");
        }
        return entity.getUserId();
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
