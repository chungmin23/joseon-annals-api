package com.spring.ai.joseonannalapi.api.controller.v1.dto.persona;

import com.spring.ai.joseonannalapi.domain.persona.Persona;

public record PersonaResponse(
        Long personaId,
        String name,
        String title,
        String reignPeriod,
        String description,
        String profileImage,
        String[] tags
) {
    public static PersonaResponse of(Persona persona) {
        return new PersonaResponse(
                persona.personaId(),
                persona.name(),
                persona.title(),
                persona.reignPeriod(),
                persona.description(),
                persona.profileImage(),
                persona.tags()
        );
    }
}
