package com.sporty.settlement.service;

import com.sporty.settlement.model.FailedPayout;
import com.sporty.settlement.model.PayoutOutboxEntry;
import com.sporty.settlement.payment.PaymentGateway;
import com.sporty.settlement.repository.FailedPayoutRepository;
import com.sporty.settlement.repository.PayoutOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutOutboxRelay {

    private static final int CHUNK_SIZE = 100;
    private static final int MAX_RETRIES = 3;

    private final PayoutOutboxRepository payoutOutboxRepository;
    private final FailedPayoutRepository failedPayoutRepository;
    private final PaymentGateway paymentGateway;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void purge() {
        payoutOutboxRepository.deleteBySentAtBefore(Instant.now().minus(1, ChronoUnit.HOURS));
    }

    // Safe for single-instance deployment (each instance has its own H2 DB).
    // Multi-instance with a shared DB would require SELECT FOR UPDATE SKIP LOCKED
    // or leader election (e.g. ShedLock) to avoid duplicate payment gateway calls.
    @Scheduled(fixedDelay = 500)
    @Transactional
    public void relay() {
        List<PayoutOutboxEntry> chunk;
        while (!(chunk = payoutOutboxRepository.findBySentAtIsNull(PageRequest.of(0, CHUNK_SIZE))).isEmpty()) {
            for (PayoutOutboxEntry entry : chunk) {
                try {
                    paymentGateway.payout(entry.getUserId(), entry.getAmount(), entry.getIdempotencyKey());
                    entry.setSentAt(Instant.now());
                    log.debug("Payout relayed for idempotencyKey={}", entry.getIdempotencyKey());
                } catch (Exception e) {
                    handleFailure(entry, e);
                }
            }
        }
    }

    private void handleFailure(PayoutOutboxEntry entry, Exception e) {
        entry.setRetryCount(entry.getRetryCount() + 1);
        log.warn("Payout attempt {}/{} failed for idempotencyKey={}",
                entry.getRetryCount(), MAX_RETRIES, entry.getIdempotencyKey(), e);

        if (entry.getRetryCount() >= MAX_RETRIES) {
            failedPayoutRepository.save(FailedPayout.builder()
                    .userId(entry.getUserId())
                    .amount(entry.getAmount())
                    .idempotencyKey(entry.getIdempotencyKey())
                    .failedAt(Instant.now())
                    .lastError(e.getMessage())
                    .build());
            payoutOutboxRepository.delete(entry);
            log.error("Payout permanently failed after {} attempts — moved to failed_payouts. idempotencyKey={}",
                    MAX_RETRIES, entry.getIdempotencyKey());
        }
    }
}
