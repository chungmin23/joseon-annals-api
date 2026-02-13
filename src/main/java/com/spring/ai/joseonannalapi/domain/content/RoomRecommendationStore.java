package com.spring.ai.joseonannalapi.domain.content;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomRecommendationStore {

    private final ConcurrentHashMap<Long, List<RecommendedContent>> store = new ConcurrentHashMap<>();

    public void put(Long roomId, List<RecommendedContent> contents) {
        store.put(roomId, contents);
    }

    public List<RecommendedContent> get(Long roomId) {
        return store.getOrDefault(roomId, List.of());
    }
}
