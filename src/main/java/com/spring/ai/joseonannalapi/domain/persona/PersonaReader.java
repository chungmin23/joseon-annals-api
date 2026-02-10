package com.spring.ai.joseonannalapi.domain.persona;

import com.spring.ai.joseonannalapi.storage.interest.UserInterestRepository;
import com.spring.ai.joseonannalapi.storage.persona.PersonaRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PersonaReader {

    private final PersonaRepository personaRepository;
    private final UserInterestRepository userInterestRepository;
    private final PersonaFinder personaFinder;

    public PersonaReader(PersonaRepository personaRepository,
                         UserInterestRepository userInterestRepository,
                         PersonaFinder personaFinder) {
        this.personaRepository = personaRepository;
        this.userInterestRepository = userInterestRepository;
        this.personaFinder = personaFinder;
    }

    public List<Persona> getRecommended(Long userId) {
        Set<String> interestValues = userInterestRepository.findByUserId(userId).stream()
                .map(e -> e.getValue().toLowerCase())
                .collect(Collectors.toSet());

        if (interestValues.isEmpty()) {
            return personaFinder.getRandom(5);
        }

        List<Persona> allPersonas = personaFinder.getAll();
        List<Persona> matched = allPersonas.stream()
                .filter(p -> p.tags() != null && Arrays.stream(p.tags())
                        .anyMatch(tag -> interestValues.contains(tag.toLowerCase())))
                .limit(5)
                .toList();

        return matched.isEmpty() ? personaFinder.getRandom(5) : matched;
    }
}
