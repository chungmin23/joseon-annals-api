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
        log.info("[추천] 시작 roomId={}, keywords={}", roomId, keywords);
        if (keywords == null || keywords.isEmpty()) {
            log.info("[추천] 키워드 없음, 스킵");
            return;
        }
        try {
            List<RecommendedContent> contents = contentFinder.findByKeywords(keywords);
            store.put(roomId, contents);
            log.info("[추천] 완료 roomId={}, 결과={}건", roomId, contents.size());
        } catch (Exception e) {
            log.warn("[추천] 실패 roomId={}: {}", roomId, e.getMessage(), e);
        }
    }
}
