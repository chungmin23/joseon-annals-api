package com.spring.ai.joseonannalapi.domain.persona;

import com.spring.ai.joseonannalapi.storage.persona.PersonaEntity;

public record Persona(
        Long personaId,
        String name,
        String title,
        String reignPeriod,
        String eraCategory,
        String description,
        String personality,
        String speakingStyle,
        String profileImage,
        String systemPrompt,
        String greeting,
        String[] tags
) {
    public static Persona from(PersonaEntity entity) {
        return new Persona(
                entity.getPersonaId(),
                entity.getName(),
                entity.getTitle(),
                entity.getReignPeriod(),
                entity.getEraCategory(),
                entity.getDescription(),
                entity.getPersonality(),
                entity.getSpeakingStyle(),
                entity.getProfileImage(),
                entity.getSystemPrompt(),
                entity.getGreeting(),
                entity.getTags()
        );
    }
}
