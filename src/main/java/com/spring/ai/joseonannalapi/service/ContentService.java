package com.spring.ai.joseonannalapi.service;

import com.spring.ai.joseonannalapi.domain.content.*;
import com.spring.ai.joseonannalapi.domain.persona.Persona;
import com.spring.ai.joseonannalapi.domain.persona.PersonaFinder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ContentService {

    private final ContentFinder contentFinder;
    private final LibraryManager libraryManager;
    private final PersonaFinder personaFinder;

    public ContentService(ContentFinder contentFinder, LibraryManager libraryManager,
                          PersonaFinder personaFinder) {
        this.contentFinder = contentFinder;
        this.libraryManager = libraryManager;
        this.personaFinder = personaFinder;
    }

    public RecommendContentsResult getRecommended(Long personaId, Long userId) {
        Persona persona = personaFinder.getById(personaId);
        String[] tags = persona.tags();

        List<RecommendedContent> youtubeList = contentFinder.findByTagsAndType(tags, ContentType.VIDEO);
        List<RecommendedContent> bookList = contentFinder.findByTagsAndType(tags, ContentType.BOOK);
        Set<Long> savedContentIds = libraryManager.getSavedContentIds(userId);
        return new RecommendContentsResult(youtubeList, bookList, savedContentIds);
    }

    public UserLibrary saveToLibrary(Long userId, Long contentId) {
        contentFinder.getById(contentId);
        return libraryManager.save(userId, contentId);
    }

    public List<LibraryContentResult> getLibrary(Long userId) {
        List<UserLibrary> libraries = libraryManager.findByUserId(userId);
        return libraries.stream()
                .map(lib -> new LibraryContentResult(lib, contentFinder.getById(lib.contentId())))
                .toList();
    }

    public void deleteFromLibrary(Long userId, Long contentId) {
        libraryManager.delete(userId, contentId);
    }

    public Set<Long> getSavedIds(Long userId) {
        return libraryManager.getSavedContentIds(userId);
    }

    public record RecommendContentsResult(
            List<RecommendedContent> youtube,
            List<RecommendedContent> books,
            Set<Long> savedContentIds
    ) {}

    public record LibraryContentResult(UserLibrary library, RecommendedContent content) {}
}
