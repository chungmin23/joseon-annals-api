package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.content.RecommendedContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentFinder {

    private static final Logger log = LoggerFactory.getLogger(ContentFinder.class);

    private final RecommendedContentRepository contentRepository;

    public ContentFinder(RecommendedContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public List<RecommendedContent> getByContentType(ContentType contentType) {
        return contentRepository.findByContentTypeOrderByDisplayOrderAsc(contentType)
                .stream()
                .map(RecommendedContent::from)
                .toList();
    }

    public RecommendedContent getById(Long contentId) {
        return contentRepository.findById(contentId)
                .map(RecommendedContent::from)
                .orElseThrow(() -> new NotFoundException("콘텐츠를 찾을 수 없습니다. contentId=" + contentId));
    }

    public List<RecommendedContent> findByKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return List.of();
        String pgArray = "{" + String.join(",", keywords) + "}";
        log.info("[ContentFinder] findByKeywords → SQL 파라미터: keywords={}", pgArray);
        List<RecommendedContent> results = contentRepository.findByKeywords(pgArray).stream()
                .map(RecommendedContent::from)
                .toList();
        log.info("[ContentFinder] findByKeywords → 결과={}건", results.size());
        return results;
    }

    public List<RecommendedContent> findByTagsAndType(String[] tags, ContentType contentType) {
        if (tags == null || tags.length == 0) {
            log.info("[ContentFinder] findByTagsAndType → tags 없음, 전체 조회 폴백 contentType={}", contentType);
            return getByContentType(contentType);
        }
        String pgArray = "{" + String.join(",", tags) + "}";
        log.info("[ContentFinder] findByTagsAndType → SQL 파라미터: keywords={}, contentType={}", pgArray, contentType);
        List<RecommendedContent> results = contentRepository
                .findByKeywordsAndContentType(pgArray, contentType.name())
                .stream()
                .map(RecommendedContent::from)
                .toList();
        if (results.isEmpty()) {
            log.info("[ContentFinder] findByTagsAndType → 태그 매칭 0건, 전체 조회 폴백 contentType={}", contentType);
            return getByContentType(contentType);
        }
        log.info("[ContentFinder] findByTagsAndType → 태그 매칭 결과={}건", results.size());
        return results;
    }

    public List<RecommendedContent> getTopPopular(int limit) {
        return contentRepository.findByIsActiveTrueOrderByPopularityScoreDesc(PageRequest.of(0, limit))
                .stream()
                .map(RecommendedContent::from)
                .toList();
    }
}
