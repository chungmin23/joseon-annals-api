package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.content.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentFinder contentFinder;

    @Mock
    private LibraryManager libraryManager;

    @InjectMocks
    private ContentService contentService;

    private RecommendedContent youtubeContent(Long contentId) {
        return new RecommendedContent(contentId, 1L, ContentType.YOUTUBE,
                "세종대왕 유튜브", "유튜브 설명", null, "https://youtube.com/watch?v=xxx", 1);
    }

    private RecommendedContent bookContent(Long contentId) {
        return new RecommendedContent(contentId, 1L, ContentType.BOOK,
                "조선왕조실록", "책 설명", null, "https://book.naver.com/xxx", 1);
    }

    private UserLibrary userLibrary(Long libraryId, Long userId, Long contentId) {
        return new UserLibrary(libraryId, userId, contentId, LocalDateTime.now());
    }

    @Test
    void getRecommended_성공() {
        // given
        Long personaId = 1L;
        Long userId = 1L;
        List<RecommendedContent> youtubeList = List.of(youtubeContent(1L));
        List<RecommendedContent> bookList = List.of(bookContent(2L));
        Set<Long> savedIds = Set.of(1L);

        given(contentFinder.getByPersonaId(personaId, ContentType.YOUTUBE)).willReturn(youtubeList);
        given(contentFinder.getByPersonaId(personaId, ContentType.BOOK)).willReturn(bookList);
        given(libraryManager.getSavedContentIds(userId)).willReturn(savedIds);

        // when
        ContentService.RecommendContentsResult result = contentService.getRecommended(personaId, userId);

        // then
        assertThat(result.youtube()).hasSize(1);
        assertThat(result.books()).hasSize(1);
        assertThat(result.savedContentIds()).containsExactly(1L);
    }

    @Test
    void saveToLibrary_성공() {
        // given
        Long userId = 1L;
        Long contentId = 5L;
        RecommendedContent content = youtubeContent(contentId);
        UserLibrary library = userLibrary(1L, userId, contentId);

        given(contentFinder.getById(contentId)).willReturn(content);
        given(libraryManager.save(userId, contentId)).willReturn(library);

        // when
        UserLibrary result = contentService.saveToLibrary(userId, contentId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.contentId()).isEqualTo(contentId);
        then(contentFinder).should().getById(contentId);
        then(libraryManager).should().save(userId, contentId);
    }

    @Test
    void getLibrary_성공() {
        // given
        Long userId = 1L;
        UserLibrary lib1 = userLibrary(1L, userId, 10L);
        UserLibrary lib2 = userLibrary(2L, userId, 20L);
        RecommendedContent content1 = youtubeContent(10L);
        RecommendedContent content2 = bookContent(20L);

        given(libraryManager.findByUserId(userId)).willReturn(List.of(lib1, lib2));
        given(contentFinder.getById(10L)).willReturn(content1);
        given(contentFinder.getById(20L)).willReturn(content2);

        // when
        List<ContentService.LibraryContentResult> result = contentService.getLibrary(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).library()).isEqualTo(lib1);
        assertThat(result.get(0).content()).isEqualTo(content1);
        assertThat(result.get(1).library()).isEqualTo(lib2);
        assertThat(result.get(1).content()).isEqualTo(content2);
    }

    @Test
    void deleteFromLibrary_성공() {
        // given
        Long userId = 1L;
        Long contentId = 5L;
        willDoNothing().given(libraryManager).delete(userId, contentId);

        // when
        contentService.deleteFromLibrary(userId, contentId);

        // then
        then(libraryManager).should().delete(userId, contentId);
    }
}
