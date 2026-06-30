package com.sporty.settlement.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    /**
     * Pay out winnings to a user.
     *
     * @param idempotencyKey stable, unique key (betId) passed to the payment
     *                       provider so retries caused by at-least-once delivery
     *                       never result in a double payout.
     */
    void payout(Long userId, BigDecimal amount, String idempotencyKey);
}
