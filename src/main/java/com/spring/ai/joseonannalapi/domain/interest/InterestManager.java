package com.spring.ai.joseonannalapi.domain.interest;

import com.spring.ai.joseonannalapi.storage.interest.UserInterestEntity;
import com.spring.ai.joseonannalapi.storage.interest.UserInterestRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InterestManager {

    private final UserInterestRepository userInterestRepository;

    public InterestManager(UserInterestRepository userInterestRepository) {
        this.userInterestRepository = userInterestRepository;
    }

    @Transactional
    public List<UserInterest> replaceAll(Long userId, List<UserInterest> interests) {
        userInterestRepository.deleteByUserId(userId);
        List<UserInterestEntity> entities = interests.stream()
                .map(i -> UserInterestEntity.create(userId, i.category(), i.value(), i.weight()))
                .toList();
        return userInterestRepository.saveAll(entities).stream()
                .map(UserInterest::from)
                .toList();
    }
}
