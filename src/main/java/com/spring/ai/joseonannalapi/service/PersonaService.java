package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.persona.PersonaReader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonaService {

    private final PersonaFinder personaFinder;
    private final PersonaReader personaReader;

    public PersonaService(PersonaFinder personaFinder, PersonaReader personaReader) {
        this.personaFinder = personaFinder;
        this.personaReader = personaReader;
    }

    public List<Persona> getAll(String era) {
        return personaFinder.getAll(era);
    }

    public Persona getById(Long personaId) {
        return personaFinder.getById(personaId);
    }

    public List<Persona> getRecommended(Long userId) {
        return personaReader.getRecommended(userId);
    }
}
