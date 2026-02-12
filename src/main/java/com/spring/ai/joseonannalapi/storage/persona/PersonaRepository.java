package com.spring.ai.joseonannalapi.storage.persona;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonaRepository extends JpaRepository<PersonaEntity, Long> {

    @Query(value = "SELECT * FROM personas ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PersonaEntity> findRandom(@Param("limit") int limit);

    @Query(value = "SELECT * FROM personas WHERE CAST(SPLIT_PART(era, '-', 1) AS INTEGER) < 1500", nativeQuery = true)
    List<PersonaEntity> findEarlyEra();

    @Query(value = "SELECT * FROM personas WHERE CAST(SPLIT_PART(era, '-', 1) AS INTEGER) BETWEEN 1500 AND 1699", nativeQuery = true)
    List<PersonaEntity> findMidEra();

    @Query(value = "SELECT * FROM personas WHERE CAST(SPLIT_PART(era, '-', 1) AS INTEGER) >= 1700", nativeQuery = true)
    List<PersonaEntity> findLateEra();
}
