package com.spring.ai.joseonannalapi.domain.persona;

import com.spring.ai.joseonannalapi.storage.chat.ChatRoomRepository;
import com.spring.ai.joseonannalapi.storage.interest.UserInterestRepository;
import com.spring.ai.joseonannalapi.storage.persona.PersonaRelationRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final PersonaRelationRepository personaRelationRepository;

    public PersonaReader(PersonaRepository personaRepository,
                         UserInterestRepository userInterestRepository,
                         PersonaFinder personaFinder,
                         ChatRoomRepository chatRoomRepository,
                         PersonaRelationRepository personaRelationRepository) {
        this.personaRepository = personaRepository;
        this.userInterestRepository = userInterestRepository;
        this.personaFinder = personaFinder;
        this.chatRoomRepository = chatRoomRepository;
        this.personaRelationRepository = personaRelationRepository;
    }

    public List<Persona> getRecommended(Long userId) {
        Set<String> interestValues = userInterestRepository.findByUserId(userId).stream()
                .map(e -> e.getValue().toLowerCase())
                .collect(Collectors.toSet());

        if (interestValues.isEmpty()) {
            return personaFinder.getRandom(5);
        }

        List<Persona> allPersonas = personaFinder.getAll(null);
        List<Persona> matched = allPersonas.stream()
                .filter(p -> p.tags() != null && Arrays.stream(p.tags())
                        .anyMatch(tag -> interestValues.contains(tag.toLowerCase())))
                .limit(5)
                .toList();

        return matched.isEmpty() ? personaFinder.getRandom(5) : matched;
    }

    public List<Persona> getDailyRecommended(Long userId) {
        List<Long> chattedPersonaIds = chatRoomRepository.findDistinctPersonaIdsByUserId(userId);

        if (chattedPersonaIds.isEmpty()) {
            // 대화내역 없음 → popularity_score 상위 2개
            return personaRepository.findTop2ByOrderByPopularityScoreDesc()
                    .stream().map(Persona::from).toList();
        }

        // 대화내역 있음 → persona_relations에서 관련 위인 조회
        List<Long> relatedIds = personaRelationRepository.findRelatedPersonaIds(chattedPersonaIds);
        if (!relatedIds.isEmpty()) {
            return personaRepository.findAllById(relatedIds).stream()
                    .limit(2)
                    .map(Persona::from)
                    .toList();
        }

        // 관련 위인 없으면 popularity_score 상위 2개로 폴백
        return personaRepository.findTop2ByOrderByPopularityScoreDesc()
                .stream().map(Persona::from).toList();
    }
}
