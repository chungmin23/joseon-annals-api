package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.user.*;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.interest.UserInterest;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@LoginUser User user) {
        User fullUser = userService.getMe(user.userId());
        return ApiResponse.success(UserResponse.of(fullUser));
    }

    @PutMapping("/me/interests")
    public ApiResponse<Void> updateInterests(@LoginUser User user,
                                              @Valid @RequestBody UpdateInterestsRequest request) {
        List<UserInterest> interests = request.interests().stream()
                .map(i -> new UserInterest(user.userId(), i.category(), i.value(), i.weight()))
                .toList();
        userService.updateInterests(user.userId(), interests);
        return ApiResponse.success();
    }

    @GetMapping("/me/stats")
    public ApiResponse<UserStatsResponse> getStats(@LoginUser User user) {
        UserService.UserStatsResult result = userService.getStats(user.userId());
        return ApiResponse.success(UserStatsResponse.of(result));
    }
}
