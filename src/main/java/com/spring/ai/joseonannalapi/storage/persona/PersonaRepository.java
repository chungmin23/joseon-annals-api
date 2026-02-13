package com.spring.ai.joseonannalapi.storage.persona;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonaRepository extends JpaRepository<PersonaEntity, Long> {

    @Query(value = "SELECT * FROM personas ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PersonaEntity> findRandom(@Param("limit") int limit);

    List<PersonaEntity> findByEraCategory(String eraCategory);

    @Query(value = "SELECT * FROM personas ORDER BY popularity_score DESC NULLS LAST LIMIT 2", nativeQuery = true)
    List<PersonaEntity> findTop2ByPopularityScore();
}
