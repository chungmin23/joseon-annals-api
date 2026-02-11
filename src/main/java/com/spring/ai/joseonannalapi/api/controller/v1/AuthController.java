package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.AuthResponse;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.LoginRequest;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.SignupRequest;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request.email(), request.password(), request.nickname()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request.email(), request.password()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
