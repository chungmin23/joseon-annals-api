package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.payment.SubscriptionResponse;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/webhook/polar")
    public ResponseEntity<Void> polarWebhookHealth() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook/polar")
    public ResponseEntity<Void> polarWebhook(
            @RequestHeader(value = "webhook-id", required = false) String webhookId,
            @RequestHeader(value = "webhook-timestamp", required = false) String timestamp,
            @RequestHeader(value = "webhook-signature", required = false) String signature,
            @RequestBody String rawBody) {
        log.info("[Payment] 웹훅 수신 webhookId={} timestamp={} signaturePresent={}",
                webhookId, timestamp, signature != null);
        if (webhookId == null || timestamp == null || signature == null) {
            log.error("[Payment] 필수 헤더 누락 webhookId={} timestamp={} signature={}",
                    webhookId, timestamp, signature);
            return ResponseEntity.badRequest().build();
        }
        paymentService.processWebhook(webhookId, timestamp, signature, rawBody);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscription")
    public ApiResponse<SubscriptionResponse> getSubscription(@LoginUser User user) {
        return ApiResponse.success(paymentService.getSubscription(user.userId()));
    }
}
