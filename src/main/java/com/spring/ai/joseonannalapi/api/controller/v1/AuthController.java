package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.AuthResponse;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.ChangePasswordRequest;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.ForgotPasswordRequest;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.GoogleLoginRequest;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.LoginRequest;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.RefreshRequest;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.auth.SignupRequest;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.user.User;
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

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.success();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ApiResponse.success();
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ApiResponse.success(authService.googleLogin(request.code(), request.redirectUri()));
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@LoginUser User user,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.userId(), request.currentPassword(), request.newPassword());
        return ApiResponse.success();
    }
}
