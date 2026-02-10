package com.spring.ai.joseonannalapi.storage.content;

import com.spring.ai.joseonannalapi.domain.content.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedContentRepository extends JpaRepository<RecommendedContentEntity, Long> {

    List<RecommendedContentEntity> findByPersonaIdAndContentTypeOrderByDisplayOrderAsc(
            Long personaId, ContentType contentType);

    List<RecommendedContentEntity> findByPersonaIdOrderByDisplayOrderAsc(Long personaId);
}
