package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import com.spring.ai.joseonannalapi.domain.persona.PersonaReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaFinder personaFinder;

    @Mock
    private PersonaReader personaReader;

    @InjectMocks
    private PersonaService personaService;

    private Persona persona(Long id, String name) {
        return new Persona(id, name, name + "왕", "1400-1450",
                name + " 설명", null, "시스템 프롬프트", new String[]{"역사"});
    }

    @Test
    void getAll_전체_페르소나_반환() {
        // given
        List<Persona> expected = List.of(
                persona(1L, "세종"),
                persona(2L, "태종"),
                persona(3L, "정조")
        );
        given(personaFinder.getAll()).willReturn(expected);

        // when
        List<Persona> result = personaService.getAll();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Persona::name)
                .containsExactly("세종", "태종", "정조");
    }

    @Test
    void getById_특정_페르소나_반환() {
        // given
        Long personaId = 1L;
        Persona expected = persona(personaId, "세종");
        given(personaFinder.getById(personaId)).willReturn(expected);

        // when
        Persona result = personaService.getById(personaId);

        // then
        assertThat(result.personaId()).isEqualTo(personaId);
        assertThat(result.name()).isEqualTo("세종");
    }

    @Test
    void getRecommended_추천_페르소나_반환() {
        // given
        Long userId = 1L;
        List<Persona> expected = List.of(
                persona(1L, "세종"),
                persona(3L, "정조")
        );
        given(personaReader.getRecommended(userId)).willReturn(expected);

        // when
        List<Persona> result = personaService.getRecommended(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Persona::personaId)
                .containsExactly(1L, 3L);
    }
}
