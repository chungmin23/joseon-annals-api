package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.content.RecommendedContentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentFinder {

    private final RecommendedContentRepository contentRepository;

    public ContentFinder(RecommendedContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public List<RecommendedContent> getByPersonaId(Long personaId, ContentType contentType) {
        return contentRepository.findByPersonaIdAndContentTypeOrderByDisplayOrderAsc(personaId, contentType)
                .stream()
                .map(RecommendedContent::from)
                .toList();
    }

    public RecommendedContent getById(Long contentId) {
        return contentRepository.findById(contentId)
                .map(RecommendedContent::from)
                .orElseThrow(() -> new NotFoundException("콘텐츠를 찾을 수 없습니다. contentId=" + contentId));
    }
}
