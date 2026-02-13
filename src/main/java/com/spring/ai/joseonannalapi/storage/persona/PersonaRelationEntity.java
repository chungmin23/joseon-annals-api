package com.spring.ai.joseonannalapi.storage.persona;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "persona_relations")
public class PersonaRelationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @Column(name = "related_persona_id", nullable = false)
    private Long relatedPersonaId;

    @Column(name = "relation_type", nullable = false, length = 50)
    private String relationType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "strength")
    private Integer strength;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
