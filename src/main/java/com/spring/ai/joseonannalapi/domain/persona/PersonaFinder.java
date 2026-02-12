package com.spring.ai.joseonannalapi.domain.persona;

import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.persona.PersonaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonaFinder {

    private final PersonaRepository personaRepository;

    public PersonaFinder(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public Persona getById(Long personaId) {
        return personaRepository.findById(personaId)
                .map(Persona::from)
                .orElseThrow(() -> new NotFoundException("페르소나를 찾을 수 없습니다. personaId=" + personaId));
    }

    public List<Persona> getAll(String era) {
        var entities = (era != null && !era.isBlank())
                ? personaRepository.findByEraCategory(era)
                : personaRepository.findAll();
        return entities.stream().map(Persona::from).toList();
    }

    public List<Persona> getRandom(int limit) {
        return personaRepository.findRandom(limit).stream()
                .map(Persona::from)
                .toList();
    }
}
