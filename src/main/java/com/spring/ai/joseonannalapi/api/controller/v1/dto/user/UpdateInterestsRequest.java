package com.spring.ai.joseonannalapi.api.controller.v1.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateInterestsRequest(@NotNull @Valid List<InterestItem> interests) {

    public record InterestItem(
            @NotNull String category,
            @NotNull String value,
            Double weight
    ) {
    }
}
