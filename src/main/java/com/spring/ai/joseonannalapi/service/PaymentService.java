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
            String subscriptionId = data.path("id").asText();
            String status = data.path("status").asText();
            String customerEmail = data.path("customer").path("email").asText();
            // metadata.userId 우선, 없으면 customer.email 폴백
            String userIdStr = data.path("metadata").path("userId").asText(null);
            log.info("[Payment] 웹훅 수신 type={} subscriptionId={} status={} email={} metaUserId={}",
                    type, subscriptionId, status, customerEmail, userIdStr);

            switch (type) {
                case "subscription.created":
                case "subscription.updated":
                    if ("active".equals(status)) {
                        upgradeToProById(userIdStr, customerEmail, subscriptionId);
                    } else {
                        downgradeToFreeById(userIdStr, customerEmail);
                    }
                    break;
                case "subscription.revoked":
                case "subscription.canceled":
                    downgradeToFreeById(userIdStr, customerEmail);
                    break;
                default:
                    log.info("[Payment] 처리하지 않는 이벤트 타입: {}", type);
            }
        } catch (Exception e) {
            log.error("[Payment] 웹훅 처리 오류: {}", e.getMessage(), e);
            throw new RuntimeException("웹훅 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public SubscriptionResponse getSubscription(Long userId) {
        UserEntity entity = userFinder.getEntityById(userId);
        boolean isPro = "PRO".equals(entity.getSubscriptionTier());
        return new SubscriptionResponse(entity.getSubscriptionTier(), isPro, entity.getDailyLimit());
    }

    private void upgradeToProById(String userIdStr, String email, String polarSubscriptionId) {
        UserEntity user = resolveUser(userIdStr, email);
        if (user == null) return;
        user.upgradeToPro(polarSubscriptionId);
        userRepository.save(user);
        log.info("[Payment] PRO 업그레이드 완료 userId={}", user.getUserId());
    }

    private void downgradeToFreeById(String userIdStr, String email) {
        UserEntity user = resolveUser(userIdStr, email);
        if (user == null) return;
        user.downgradeToFree();
        userRepository.save(user);
        log.info("[Payment] FREE 다운그레이드 완료 userId={}", user.getUserId());
    }

    private UserEntity resolveUser(String userIdStr, String email) {
        // 1) metadata.userId 로 조회 (가장 신뢰성 높음)
        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                return userRepository.findById(userId).orElse(null);
            } catch (NumberFormatException e) {
                log.warn("[Payment] metadata.userId 파싱 실패: {}", userIdStr);
            }
        }
        // 2) 폴백: customer.email 로 조회
        if (email != null && !email.isBlank()) {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) return userOpt.get();
            log.warn("[Payment] 사용자 없음 — userId={} email={}", userIdStr, email);
        }
        return null;
    }

    private void verifySignature(String webhookId, String timestamp,
                                  String signature, String rawBody) {
        try {
            // Polar/Svix: secret은 "whsec_<base64>" 형태
            String base64Secret = webhookSecret.replace("whsec_", "");
            log.info("[Payment] 시그니처 검증 시작 — secret 앞 8자: {}***",
                    webhookSecret.substring(0, Math.min(8, webhookSecret.length())));

            byte[] secretBytes;
            try {
                secretBytes = Base64.getDecoder().decode(base64Secret);
            } catch (IllegalArgumentException e) {
                log.error("[Payment] Base64 디코딩 실패 — POLAR_WEBHOOK_SECRET 값 확인 필요: {}", e.getMessage());
                throw e;
            }

            String signedContent = webhookId + "." + timestamp + "." + rawBody;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            String computed = Base64.getEncoder().encodeToString(
                    mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8)));

            log.info("[Payment] 계산된 시그니처 앞 16자: {}***", computed.substring(0, Math.min(16, computed.length())));

            // signature 헤더: 공백으로 구분된 "v1,<base64>" 목록
            boolean valid = Arrays.stream(signature.split(" "))
                    .anyMatch(s -> s.equals("v1," + computed));

            if (!valid) {
                log.error("[Payment] 웹훅 시그니처 검증 실패 — received: {}", signature);
                throw new IllegalArgumentException("Invalid webhook signature");
            }
            log.info("[Payment] 시그니처 검증 성공");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Payment] 시그니처 검증 중 오류: {}", e.getMessage());
            throw new RuntimeException("Signature verification failed", e);
        }
    }
}
