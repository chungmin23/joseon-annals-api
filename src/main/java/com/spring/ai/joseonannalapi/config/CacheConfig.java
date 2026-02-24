package com.spring.ai.joseonannalapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // 페르소나 목록 — 변경 없는 데이터, 1시간 유지
        CaffeineCache personasCache = new CaffeineCache("personas",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .build());

        // 페르소나 단건 — 채팅마다 반복 조회, 1시간 유지
        CaffeineCache personaCache = new CaffeineCache("persona",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .build());

        // 페르소나별 추천 콘텐츠 — 모든 유저 동일 결과, 30분 유지
        CaffeineCache recommendedContentsCache = new CaffeineCache("recommendedContents",
                Caffeine.newBuilder()
                        .maximumSize(50)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .build());

        // 일일 사용량 — 메시지 전송마다 조회, 1분 유지 (정합성 고려)
        CaffeineCache dailyUsageCache = new CaffeineCache("dailyUsage",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .build());

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                personasCache,
                personaCache,
                recommendedContentsCache,
                dailyUsageCache
        ));
        return cacheManager;
    }
}
