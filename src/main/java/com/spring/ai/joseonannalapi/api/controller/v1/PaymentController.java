package com.spring.ai.joseonannalapi.api.controller.v1;

import com.spring.ai.joseonannalapi.api.controller.v1.dto.payment.SubscriptionResponse;
import com.spring.ai.joseonannalapi.api.support.LoginUser;
import com.spring.ai.joseonannalapi.common.ApiResponse;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook/polar")
    public ResponseEntity<Void> polarWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String timestamp,
            @RequestHeader("webhook-signature") String signature,
            @RequestBody String rawBody) {
        paymentService.processWebhook(webhookId, timestamp, signature, rawBody);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscription")
    public ApiResponse<SubscriptionResponse> getSubscription(@LoginUser User user) {
        return ApiResponse.success(paymentService.getSubscription(user.userId()));
    }
}
