package com.spring.ai.joseonannalapi.domain.user;

import com.spring.ai.joseonannalapi.storage.user.UserEntity;

import java.time.LocalDateTime;

public record User(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        LocalDateTime createdAt
) {
    public static User from(UserEntity entity) {
        return new User(
                entity.getUserId(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getProfileImage(),
                entity.getCreatedAt()
        );
    }
}
