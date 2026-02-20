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

            log.info("[Payment] 웹훅 수신 type={} subscriptionId={} status={} email={}",
                    type, subscriptionId, status, customerEmail);

            switch (type) {
                case "subscription.created":
                case "subscription.updated":
                    if ("active".equals(status)) {
                        upgradeToPro(customerEmail, subscriptionId);
                    } else {
                        downgradeToFree(customerEmail);
                    }
                    break;
                case "subscription.revoked":
                case "subscription.canceled":
                    downgradeToFree(customerEmail);
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

    private void upgradeToPro(String email, String polarSubscriptionId) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("[Payment] 업그레이드 실패 — 이메일 없음: {}", email);
            return;
        }
        UserEntity user = userOpt.get();
        user.upgradeToPro(polarSubscriptionId);
        userRepository.save(user);
        log.info("[Payment] PRO 업그레이드 완료 userId={} email={}", user.getUserId(), email);
    }

    private void downgradeToFree(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("[Payment] 다운그레이드 실패 — 이메일 없음: {}", email);
            return;
        }
        UserEntity user = userOpt.get();
        user.downgradeToFree();
        userRepository.save(user);
        log.info("[Payment] FREE 다운그레이드 완료 userId={} email={}", user.getUserId(), email);
    }

    private void verifySignature(String webhookId, String timestamp,
                                  String signature, String rawBody) {
        try {
            // Polar/Svix: secret은 "whsec_<base64>" 형태
            String base64Secret = webhookSecret.replace("whsec_", "");
            byte[] secretBytes = Base64.getDecoder().decode(base64Secret);

            String signedContent = webhookId + "." + timestamp + "." + rawBody;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            String computed = Base64.getEncoder().encodeToString(
                    mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8)));

            // signature 헤더: 공백으로 구분된 "v1,<base64>" 목록
            boolean valid = Arrays.stream(signature.split(" "))
                    .anyMatch(s -> s.equals("v1," + computed));

            if (!valid) {
                log.error("[Payment] 웹훅 시그니처 검증 실패");
                throw new IllegalArgumentException("Invalid webhook signature");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Payment] 시그니처 검증 중 오류: {}", e.getMessage());
            throw new RuntimeException("Signature verification failed", e);
        }
    }
}
