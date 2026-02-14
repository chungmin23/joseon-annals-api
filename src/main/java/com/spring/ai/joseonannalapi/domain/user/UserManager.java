package com.spring.ai.joseonannalapi.domain.user;

import com.spring.ai.joseonannalapi.common.exception.DuplicateException;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import com.spring.ai.joseonannalapi.storage.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserManager {

    private final UserRepository userRepository;
    private final UserFinder userFinder;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserManager(UserRepository userRepository, UserFinder userFinder,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFinder = userFinder;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword, String nickname) {
        if (userFinder.existsByEmail(email)) {
            throw new DuplicateException("이미 사용 중인 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        UserEntity entity = UserEntity.create(email, encodedPassword, nickname);
        UserEntity saved = userRepository.save(entity);
        return User.from(saved);
    }

    @Transactional
    public User findOrCreateGoogleUser(String email, String nickname, String profileImage) {
        if (userFinder.existsByEmail(email)) {
            return User.from(userFinder.getEntityByEmail(email));
        }
        UserEntity entity = UserEntity.createWithGoogle(email, nickname, profileImage);
        return User.from(userRepository.save(entity));
    }
}
