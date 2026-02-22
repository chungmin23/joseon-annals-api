package com.spring.ai.joseonannalapi.api.controller.v1.dto.payment;

public record SubscriptionResponse(
        String tier,
        boolean isPro,
        int dailyLimit,
        boolean cancelAtPeriodEnd
) {}
