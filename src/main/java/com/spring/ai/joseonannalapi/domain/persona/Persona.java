package com.spring.ai.joseonannalapi.domain.persona;

import com.spring.ai.joseonannalapi.storage.persona.PersonaEntity;

public record Persona(
        Long personaId,
        String name,
        String title,
        String reignPeriod,
        String description,
        String profileImage,
        String systemPrompt,
        String[] tags
) {
    public static Persona from(PersonaEntity entity) {
        return new Persona(
                entity.getPersonaId(),
                entity.getName(),
                entity.getTitle(),
                entity.getReignPeriod(),
                entity.getDescription(),
                entity.getProfileImage(),
                entity.getSystemPrompt(),
                entity.getTags()
        );
    }
}
