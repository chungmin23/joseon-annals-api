package com.spring.ai.joseonannalapi.domain.content;

import com.spring.ai.joseonannalapi.common.exception.DuplicateException;
import com.spring.ai.joseonannalapi.common.exception.NotFoundException;
import com.spring.ai.joseonannalapi.storage.content.UserLibraryEntity;
import com.spring.ai.joseonannalapi.storage.content.UserLibraryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LibraryManager {

    private final UserLibraryRepository userLibraryRepository;

    public LibraryManager(UserLibraryRepository userLibraryRepository) {
        this.userLibraryRepository = userLibraryRepository;
    }

    @Transactional
    public UserLibrary save(Long userId, Long contentId) {
        if (userLibraryRepository.existsByUserIdAndContentId(userId, contentId)) {
            throw new DuplicateException("이미 라이브러리에 저장된 콘텐츠입니다.");
        }
        UserLibraryEntity entity = UserLibraryEntity.create(userId, contentId);
        return UserLibrary.from(userLibraryRepository.save(entity));
    }

    @Transactional
    public void delete(Long userId, Long contentId) {
        if (!userLibraryRepository.existsByUserIdAndContentId(userId, contentId)) {
            throw new NotFoundException("라이브러리에 저장되지 않은 콘텐츠입니다.");
        }
        userLibraryRepository.deleteByUserIdAndContentId(userId, contentId);
    }

    public List<UserLibrary> findByUserId(Long userId) {
        return userLibraryRepository.findByUserId(userId).stream()
                .map(UserLibrary::from)
                .toList();
    }

    public Set<Long> getSavedContentIds(Long userId) {
        return userLibraryRepository.findByUserId(userId).stream()
                .map(e -> e.getContentId())
                .collect(Collectors.toSet());
    }
}
