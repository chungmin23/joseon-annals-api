package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.content.RecommendedContentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentFinder {

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
        return contentRepository.findByKeywords(pgArray).stream()
                .map(RecommendedContent::from)
                .toList();
    }

    public List<RecommendedContent> findByTagsAndType(String[] tags, ContentType contentType) {
        if (tags == null || tags.length == 0) {
            return getByContentType(contentType);
        }
        String pgArray = "{" + String.join(",", tags) + "}";
        List<RecommendedContent> results = contentRepository
                .findByKeywordsAndContentType(pgArray, contentType.name())
                .stream()
                .map(RecommendedContent::from)
                .toList();
        return results.isEmpty() ? getByContentType(contentType) : results;
    }

    public List<RecommendedContent> getTopPopular(int limit) {
        return contentRepository.findByIsActiveTrueOrderByPopularityScoreDesc(PageRequest.of(0, limit))
                .stream()
                .map(RecommendedContent::from)
                .toList();
    }
}
