package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.persona.PersonaResponse;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.service.PersonaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    public ApiResponse<List<PersonaResponse>> getAll(
            @RequestParam(required = false) String era) {
        List<PersonaResponse> responses = personaService.getAll(era).stream()
                .map(PersonaResponse::of)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/recommend")
    public ApiResponse<List<PersonaResponse>> getRecommended(@LoginUser User user) {
        List<PersonaResponse> responses = personaService.getRecommended(user.userId()).stream()
                .map(PersonaResponse::of)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/daily")
    public ApiResponse<List<PersonaResponse>> getDailyRecommended(@LoginUser User user) {
        List<PersonaResponse> responses = personaService.getDailyRecommended(user.userId()).stream()
                .map(PersonaResponse::of)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{personaId}")
    public ApiResponse<PersonaResponse> getById(@PathVariable Long personaId) {
        return ApiResponse.success(PersonaResponse.of(personaService.getById(personaId)));
    }
}
