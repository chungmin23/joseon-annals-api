package com.spring.ai.joseonannalapi.storage.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "provider", length = 20)
    private String provider;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "onboarding_tags", columnDefinition = "TEXT[]")
    private String[] onboardingTags;

    @Column(name = "preferred_era", length = 50)
    private String preferredEra;

    @Column(name = "learning_purpose", columnDefinition = "TEXT")
    private String learningPurpose;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "daily_limit", nullable = false)
    private int dailyLimit = 10;

    @Column(name = "subscription_tier", nullable = false, length = 20)
    private String subscriptionTier = "FREE";

    @Column(name = "polar_subscription_id", length = 200)
    private String polarSubscriptionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Map<String, Object> preferences;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static UserEntity create(String email, String encodedPassword, String nickname) {
        UserEntity entity = new UserEntity();
        entity.email = email;
        entity.password = encodedPassword;
        entity.nickname = nickname;
        entity.provider = "EMAIL";
        return entity;
    }

    public static UserEntity createWithGoogle(String email, String nickname, String profileImage) {
        UserEntity entity = new UserEntity();
        entity.email = email;
        entity.nickname = nickname;
        entity.profileImage = profileImage;
        entity.provider = "GOOGLE";
        return entity;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void upgradeToPro(String polarSubscriptionId) {
        this.subscriptionTier = "PRO";
        this.dailyLimit = 100;
        this.polarSubscriptionId = polarSubscriptionId;
    }

    public void downgradeToFree() {
        this.subscriptionTier = "FREE";
        this.dailyLimit = 10;
        this.polarSubscriptionId = null;
    }
}
