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
        log.info("[추천] ===== 채팅 키워드 추천 시작 =====");
        log.info("[추천] roomId={}, 키워드 수={}, keywords={}", roomId,
                keywords != null ? keywords.size() : 0, keywords);
        try {
            List<RecommendedContent> contents = List.of();
            if (keywords != null && !keywords.isEmpty()) {
                String pgArray = "{" + String.join(",", keywords) + "}";
                log.info("[추천] DB 쿼리 파라미터: keywords={}", pgArray);
                contents = contentFinder.findByKeywords(keywords);
                log.info("[추천] 키워드 매칭 결과={}건", contents.size());
                contents.forEach(c ->
                        log.info("[추천] 결과 콘텐츠: id={}, type={}, title={}", c.contentId(), c.contentType(), c.title()));
            } else {
                log.info("[추천] 키워드 없음 → 추천 스킵");
            }
            store.put(roomId, contents);
            log.info("[추천] ===== 완료 roomId={}, 저장 건수={} =====", roomId, contents.size());
        } catch (Exception e) {
            log.warn("[추천] 실패 roomId={}: {}", roomId, e.getMessage(), e);
        }
    }
}
