package com.spring.ai.joseonannalapi.domain.user;

import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import com.spring.ai.joseonannalapi.storage.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserFinder {

    private final UserRepository userRepository;

    public UserFinder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(Long userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));
        return User.from(entity);
    }

    public UserEntity getEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));
    }

    public UserEntity getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. email=" + email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
