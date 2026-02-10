package com.spring.ai.joseonannalapi.api.controller.v1.dto.content;

import java.util.List;

public record RecommendContentsResponse(
        List<ContentItemResponse> youtube,
        List<ContentItemResponse> books
) {
}
