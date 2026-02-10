package com.spring.ai.joseonannalapi.api.controller.v1.dto.user;

import com.spring.ai.joseonannalapi.domain.user.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        LocalDateTime createdAt
) {
    public static UserResponse of(User user) {
        return new UserResponse(
                user.userId(),
                user.email(),
                user.nickname(),
                user.profileImage(),
                user.createdAt()
        );
    }
}
