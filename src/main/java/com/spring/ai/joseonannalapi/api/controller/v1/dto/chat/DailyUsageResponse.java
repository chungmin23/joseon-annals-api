package com.spring.ai.joseonannalapi.api.controller.v1.dto.chat;

public record DailyUsageResponse(
        long usedCount,
        long limitCount,
        long remainingCount
) {
}
