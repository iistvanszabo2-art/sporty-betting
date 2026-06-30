package com.sporty.matcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.matcher.dto.BetSettlementDto;
import com.sporty.matcher.model.FailedOutboxEntry;
import com.sporty.matcher.model.OutboxEntry;
import com.sporty.matcher.repository.FailedOutboxRepository;
import com.sporty.matcher.repository.OutboxRepository;
import com.sporty.matcher.rocketmq.BetSettlementProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
public class OutboxRelay {

    private static final int CHUNK_SIZE = 100;
    private static final int MAX_RETRIES = 3;

    private final OutboxRepository outboxRepository;
    private final FailedOutboxRepository failedOutboxRepository;
    private final BetSettlementProducer betSettlementProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void purge() {
        outboxRepository.deleteBySentAtBefore(Instant.now().minus(1, ChronoUnit.HOURS));
    }

    // Safe for single-instance deployment (each instance has its own H2 DB).
    // Multi-instance with a shared DB would require SELECT FOR UPDATE SKIP LOCKED
    // or leader election (e.g. ShedLock) to avoid duplicate RocketMQ publishes.
    @Scheduled(fixedDelay = 500)
    @Transactional
    public void relay() {
        List<OutboxEntry> chunk;
        while (!(chunk = outboxRepository.findBySentAtIsNull(PageRequest.of(0, CHUNK_SIZE))).isEmpty()) {
            for (OutboxEntry entry : chunk) {
                try {
                    BetSettlementDto dto = objectMapper.readValue(entry.getPayload(), BetSettlementDto.class);
                    if (entry.getCorrelationId() != null) {
                        MDC.put("correlationId", entry.getCorrelationId());
                    }
                    betSettlementProducer.send(dto);
                    entry.setSentAt(Instant.now());
                    log.debug("Relayed outbox entry id={} betId={}", entry.getId(), entry.getBetId());
                } catch (Exception e) {
                    handleFailure(entry, e);
                } finally {
                    MDC.remove("correlationId");
                }
            }
        }
    }

    private void handleFailure(OutboxEntry entry, Exception e) {
        entry.setRetryCount(entry.getRetryCount() + 1);
        log.warn("Relay attempt {}/{} failed for outbox entry id={} betId={}",
                entry.getRetryCount(), MAX_RETRIES, entry.getId(), entry.getBetId(), e);

        if (entry.getRetryCount() >= MAX_RETRIES) {
            failedOutboxRepository.save(FailedOutboxEntry.builder()
                    .betId(entry.getBetId())
                    .payload(entry.getPayload())
                    .failedAt(Instant.now())
                    .lastError(e.getMessage())
                    .build());
            outboxRepository.delete(entry);
            log.error("Outbox entry permanently failed after {} attempts — moved to failed_outbox. id={} betId={}",
                    MAX_RETRIES, entry.getId(), entry.getBetId());
        }
    }
}
