package com.sporty.outcome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.outcome.config.CorrelationFilter;
import com.sporty.outcome.dto.EventOutcomeDto;
import com.sporty.outcome.kafka.EventOutcomeProducer;
import com.sporty.outcome.model.FailedOutboxEntry;
import com.sporty.outcome.model.OutboxEntry;
import com.sporty.outcome.repository.FailedOutboxRepository;
import com.sporty.outcome.repository.OutboxRepository;
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
    private final EventOutcomeProducer producer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void purge() {
        outboxRepository.deleteBySentAtBefore(Instant.now().minus(1, ChronoUnit.HOURS));
    }

    // Safe for single-instance deployment (each instance has its own H2 DB).
    // Multi-instance with a shared DB would require SELECT FOR UPDATE SKIP LOCKED
    // or leader election (e.g. ShedLock) to avoid duplicate Kafka publishes.
    @Scheduled(fixedDelay = 500)
    @Transactional
    public void relay() {
        List<OutboxEntry> chunk;
        while (!(chunk = outboxRepository.findBySentAtIsNull(PageRequest.of(0, CHUNK_SIZE))).isEmpty()) {
            for (OutboxEntry entry : chunk) {
                try {
                    EventOutcomeDto dto = objectMapper.readValue(entry.getPayload(), EventOutcomeDto.class);
                    if (entry.getCorrelationId() != null) {
                        MDC.put(CorrelationFilter.MDC_KEY, entry.getCorrelationId());
                    }
                    producer.publish(dto);
                    entry.setSentAt(Instant.now());
                    log.debug("Relayed outbox entry id={} eventId={}", entry.getId(), entry.getEventId());
                } catch (Exception e) {
                    handleFailure(entry, e);
                } finally {
                    MDC.remove(CorrelationFilter.MDC_KEY);
                }
            }
        }
    }

    private void handleFailure(OutboxEntry entry, Exception e) {
        entry.setRetryCount(entry.getRetryCount() + 1);
        log.warn("Relay attempt {}/{} failed for outbox entry id={} eventId={}",
                entry.getRetryCount(), MAX_RETRIES, entry.getId(), entry.getEventId(), e);

        if (entry.getRetryCount() >= MAX_RETRIES) {
            failedOutboxRepository.save(FailedOutboxEntry.builder()
                    .eventId(entry.getEventId())
                    .payload(entry.getPayload())
                    .failedAt(Instant.now())
                    .lastError(e.getMessage())
                    .build());
            outboxRepository.delete(entry);
            log.error("Outbox entry permanently failed after {} attempts — moved to failed_outbox. id={} eventId={}",
                    MAX_RETRIES, entry.getId(), entry.getEventId());
        }
    }
}
