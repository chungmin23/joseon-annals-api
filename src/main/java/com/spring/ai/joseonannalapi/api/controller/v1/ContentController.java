package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.content.*;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/recommend/{personaId}")
    public ApiResponse<RecommendContentsResponse> getRecommended(@LoginUser User user,
                                                                  @PathVariable Long personaId) {
        ContentService.RecommendContentsResult result = contentService.getRecommended(personaId, user.userId());

        List<ContentItemResponse> youtube = result.youtube().stream()
                .map(c -> ContentItemResponse.of(c, result.savedContentIds()))
                .toList();
        List<ContentItemResponse> books = result.books().stream()
                .map(c -> ContentItemResponse.of(c, result.savedContentIds()))
                .toList();

        return ApiResponse.success(new RecommendContentsResponse(youtube, books));
    }

    @PostMapping("/library")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> saveToLibrary(@LoginUser User user,
                                            @Valid @RequestBody SaveLibraryRequest request) {
        contentService.saveToLibrary(user.userId(), request.contentId());
        return ApiResponse.success();
    }

    @GetMapping("/library")
    public ApiResponse<List<ContentItemResponse>> getLibrary(@LoginUser User user) {
        List<ContentItemResponse> responses = contentService.getLibrary(user.userId()).stream()
                .map(result -> {
                    var content = result.content();
                    return ContentItemResponse.of(content, java.util.Set.of(content.contentId()));
                })
                .toList();
        return ApiResponse.success(responses);
    }

    @DeleteMapping("/library/{contentId}")
    public ApiResponse<Void> deleteFromLibrary(@LoginUser User user, @PathVariable Long contentId) {
        contentService.deleteFromLibrary(user.userId(), contentId);
        return ApiResponse.success();
    }
}
