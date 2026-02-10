package com.spring.ai.joseonannalapi.storage.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLibraryRepository extends JpaRepository<UserLibraryEntity, Long> {

    List<UserLibraryEntity> findByUserId(Long userId);

    Optional<UserLibraryEntity> findByUserIdAndContentId(Long userId, Long contentId);

    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    void deleteByUserIdAndContentId(Long userId, Long contentId);
}
