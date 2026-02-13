package com.spring.ai.joseonannalapi.storage.persona;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonaRelationRepository extends JpaRepository<PersonaRelationEntity, Long> {

    @Query("SELECT pr.relatedPersonaId FROM PersonaRelationEntity pr WHERE pr.personaId IN :personaIds ORDER BY pr.strength DESC NULLS LAST")
    List<Long> findRelatedPersonaIds(@Param("personaIds") List<Long> personaIds);
}
