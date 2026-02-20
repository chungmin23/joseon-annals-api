package com.spring.ai.joseonannalapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ai.joseonannalapi.api.controller.v1.dto.payment.SubscriptionResponse;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import com.spring.ai.joseonannalapi.storage.user.UserEntity;
import com.spring.ai.joseonannalapi.storage.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final UserFinder userFinder;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${polar.webhook-secret:whsec_placeholder}")
    private String webhookSecret;

    public PaymentService(UserFinder userFinder, UserRepository userRepository,
                          ObjectMapper objectMapper) {
        this.userFinder = userFinder;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processWebhook(String webhookId, String timestamp,
                               String signature, String rawBody) {
        verifySignature(webhookId, timestamp, signature, rawBody);

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String type = root.path("type").asText();
            JsonNode data = root.path("data");

            String subscriptionId = normalizeText(data.path("id").asText(null));
            String status = normalizeText(data.path("status").asText(null));
            String customerEmail = extractCustomerEmail(data);
            String userIdStr = extractUserId(data);

            log.info("[Payment] webhook received type={} subscriptionId={} status={} email={} metaUserId={}",
                    type, subscriptionId, status, customerEmail, userIdStr);

            switch (type) {
                case "subscription.created":
                case "subscription.updated":
                    if (isProStatus(status)) {
                        upgradeToPro(userIdStr, customerEmail, subscriptionId);
                    } else if (isDowngradeStatus(status)) {
                        downgradeToFree(userIdStr, customerEmail, subscriptionId);
                    } else {
                        log.info("[Payment] skipped due to pending status={} subscriptionId={}",
                                status, subscriptionId);
                    }
                    break;
                case "subscription.revoked":
                case "subscription.canceled":
                    downgradeToFree(userIdStr, customerEmail, subscriptionId);
                    break;
                default:
                    log.info("[Payment] unsupported event type={}", type);
            }
        } catch (Exception e) {
            log.error("[Payment] webhook handling failed: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage(), e);
        }
    }

    public SubscriptionResponse getSubscription(Long userId) {
        UserEntity entity = userFinder.getEntityById(userId);
        boolean isPro = "PRO".equals(entity.getSubscriptionTier());
        return new SubscriptionResponse(entity.getSubscriptionTier(), isPro, entity.getDailyLimit());
    }

    private void upgradeToPro(String userIdStr, String email, String polarSubscriptionId) {
        UserEntity user = resolveUser(userIdStr, email, polarSubscriptionId);
        if (user == null) {
            log.warn("[Payment] upgrade skipped; user mapping failed userId={} email={} subscriptionId={}",
                    userIdStr, email, polarSubscriptionId);
            return;
        }

        if ("PRO".equals(user.getSubscriptionTier())
                && polarSubscriptionId != null
                && polarSubscriptionId.equals(user.getPolarSubscriptionId())) {
            log.info("[Payment] already PRO userId={} subscriptionId={}",
                    user.getUserId(), polarSubscriptionId);
            return;
        }

        user.upgradeToPro(polarSubscriptionId);
        userRepository.save(user);
        log.info("[Payment] upgraded to PRO userId={} email={}", user.getUserId(), user.getEmail());
    }

    private void downgradeToFree(String userIdStr, String email, String polarSubscriptionId) {
        UserEntity user = resolveUser(userIdStr, email, polarSubscriptionId);
        if (user == null) {
            log.warn("[Payment] downgrade skipped; user mapping failed userId={} email={} subscriptionId={}",
                    userIdStr, email, polarSubscriptionId);
            return;
        }

        if ("FREE".equals(user.getSubscriptionTier())) {
            log.info("[Payment] already FREE userId={}", user.getUserId());
            return;
        }

        user.downgradeToFree();
        userRepository.save(user);
        log.info("[Payment] downgraded to FREE userId={} email={}", user.getUserId(), user.getEmail());
    }

    private UserEntity resolveUser(String userIdStr, String email, String polarSubscriptionId) {
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                Optional<UserEntity> byId = userRepository.findById(userId);
                if (byId.isPresent()) {
                    return byId.get();
                }
                log.warn("[Payment] userId mapping failed userId={}", userId);
            } catch (NumberFormatException e) {
                log.warn("[Payment] userId parsing failed userId={}", userIdStr);
            }
        }

        if (polarSubscriptionId != null) {
            Optional<UserEntity> bySubscriptionId = userRepository.findByPolarSubscriptionId(polarSubscriptionId);
            if (bySubscriptionId.isPresent()) {
                return bySubscriptionId.get();
            }
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null) {
            Optional<UserEntity> byEmail = userRepository.findByEmailIgnoreCase(normalizedEmail);
            if (byEmail.isPresent()) {
                return byEmail.get();
            }
        }

        return null;
    }

    private String extractCustomerEmail(JsonNode data) {
        String[] candidatePaths = {
                "customer.email",
                "customer_email",
                "user.email",
                "email"
        };

        for (String path : candidatePaths) {
            String value = normalizeEmail(readText(data, path));
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String extractUserId(JsonNode data) {
        String[] candidatePaths = {
                "metadata.userId",
                "metadata.user_id",
                "metadata.userid",
                "customer.external_id"
        };

        for (String path : candidatePaths) {
            String value = normalizeText(readText(data, path));
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String readText(JsonNode root, String dotPath) {
        String[] parts = dotPath.split("\\.");
        JsonNode cursor = root;
        for (String part : parts) {
            if (cursor == null || cursor.isMissingNode() || cursor.isNull()) {
                return null;
            }
            cursor = cursor.path(part);
        }

        if (cursor == null || cursor.isMissingNode() || cursor.isNull()) {
            return null;
        }
        return cursor.asText(null);
    }

    private boolean isProStatus(String status) {
        return "active".equals(status) || "trialing".equals(status);
    }

    private boolean isDowngradeStatus(String status) {
        return "canceled".equals(status)
                || "cancelled".equals(status)
                || "revoked".equals(status)
                || "expired".equals(status)
                || "incomplete_expired".equals(status)
                || "unpaid".equals(status)
                || "past_due".equals(status);
    }

    private String normalizeEmail(String email) {
        String value = normalizeText(email);
        if (value == null || !value.contains("@")) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void verifySignature(String webhookId, String timestamp,
                                 String signature, String rawBody) {
        try {
            String base64Secret = webhookSecret.replace("whsec_", "");
            byte[] secretBytes = Base64.getDecoder().decode(base64Secret);
            String signedContent = webhookId + "." + timestamp + "." + rawBody;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            String computed = Base64.getEncoder().encodeToString(
                    mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8)));

            boolean valid = Arrays.stream(signature.split(" "))
                    .anyMatch(s -> s.equals("v1," + computed));

            if (!valid) {
                throw new IllegalArgumentException("Invalid webhook signature");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }
}
