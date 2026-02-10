package com.spring.ai.joseonannalapi.storage.interest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterestEntity, Long> {

    List<UserInterestEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
