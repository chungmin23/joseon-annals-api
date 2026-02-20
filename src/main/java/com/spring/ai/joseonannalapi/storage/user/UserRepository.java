package com.spring.ai.joseonannalapi.storage.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByPolarSubscriptionId(String polarSubscriptionId);

    boolean existsByEmail(String email);
}
