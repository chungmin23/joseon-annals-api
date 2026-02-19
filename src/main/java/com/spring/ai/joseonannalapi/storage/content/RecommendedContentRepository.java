package com.spring.ai.joseonannalapi.storage.content;

import com.spring.ai.joseonannalapi.domain.content.ContentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendedContentRepository extends JpaRepository<RecommendedContentEntity, Long> {

    List<RecommendedContentEntity> findByContentTypeOrderByDisplayOrderAsc(ContentType contentType);

    @Query(value = """
            SELECT * FROM recommended_contents
            WHERE is_active = true
              AND (
                EXISTS (
                    SELECT 1
                    FROM unnest(tags) AS t, unnest(CAST(:keywords AS TEXT[])) AS kw
                    WHERE t ILIKE '%' || kw || '%'
                )
                OR category = ANY(CAST(:keywords AS TEXT[]))
              )
            ORDER BY popularity_score DESC
            LIMIT 6
            """, nativeQuery = true)
    List<RecommendedContentEntity> findByKeywords(@Param("keywords") String keywords);

    @Query(value = """
            SELECT * FROM recommended_contents
            WHERE is_active = true
              AND content_type = :contentType
              AND (
                EXISTS (
                    SELECT 1
                    FROM unnest(tags) AS t, unnest(CAST(:keywords AS TEXT[])) AS kw
                    WHERE t ILIKE '%' || kw || '%'
                )
                OR category = ANY(CAST(:keywords AS TEXT[]))
              )
            ORDER BY popularity_score DESC
            LIMIT 6
            """, nativeQuery = true)
    List<RecommendedContentEntity> findByKeywordsAndContentType(
            @Param("keywords") String keywords,
            @Param("contentType") String contentType);

    List<RecommendedContentEntity> findByIsActiveTrueOrderByPopularityScoreDesc(Pageable pageable);
}
