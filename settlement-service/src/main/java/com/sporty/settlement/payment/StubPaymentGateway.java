package com.sporty.settlement.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public void payout(Long userId, BigDecimal amount, String idempotencyKey) {
        // In production: POST /v1/payouts to Stripe (or equivalent) with
        // Idempotency-Key header set to idempotencyKey. The provider deduplicates
        // on its side, so a retry from at-least-once delivery is a no-op there too.
        log.info("PAYOUT [stub] userId={} amount={} idempotencyKey={}", userId, amount, idempotencyKey);
    }
}
