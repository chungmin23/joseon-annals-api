package com.spring.ai.joseonannalapi.domain.interest;

import com.spring.ai.joseonannalapi.storage.interest.UserInterestEntity;

public record UserInterest(
        Long userId,
        String category,
        String value,
        Double weight
) {
    public static UserInterest from(UserInterestEntity entity) {
        return new UserInterest(
                entity.getUserId(),
                entity.getCategory(),
                entity.getValue(),
                entity.getWeight()
        );
    }
}
