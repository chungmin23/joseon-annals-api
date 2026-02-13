package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.content.ContentFinder;
import com.spring.ai.joseonannalapi.domain.content.RecommendedContent;
import com.spring.ai.joseonannalapi.domain.content.RoomRecommendationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(ContentRecommendationService.class);

    private final ContentFinder contentFinder;
    private final RoomRecommendationStore store;

    public ContentRecommendationService(ContentFinder contentFinder, RoomRecommendationStore store) {
        this.contentFinder = contentFinder;
        this.store = store;
    }

    @Async
    public void updateRecommendations(Long roomId, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;
        try {
            List<RecommendedContent> contents = contentFinder.findByKeywords(keywords);
            store.put(roomId, contents);
        } catch (Exception e) {
            log.warn("추천 콘텐츠 업데이트 실패 roomId={}: {}", roomId, e.getMessage());
        }
    }
}
