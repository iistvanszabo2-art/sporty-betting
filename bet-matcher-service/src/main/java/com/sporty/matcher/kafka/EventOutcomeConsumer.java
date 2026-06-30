package com.sporty.matcher.kafka;

import com.sporty.matcher.dto.EventOutcomeDto;
import com.sporty.matcher.service.BetMatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeConsumer {

    static final String CORRELATION_ID_HEADER = "correlationId";

    private final BetMatcherService betMatcherService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "event-outcomes", groupId = "bet-matcher-group")
    public void consume(EventOutcomeDto outcome,
                        @Header(value = CORRELATION_ID_HEADER, required = false) byte[] correlationIdBytes) {
        String correlationId = correlationIdBytes != null
                ? new String(correlationIdBytes, StandardCharsets.UTF_8)
                : UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_HEADER, correlationId);
        try {
            log.info("Received event outcome from Kafka: {}", outcome);
            betMatcherService.processEventOutcome(outcome, correlationId);
        } finally {
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }

    @DltHandler
    public void handleDlt(EventOutcomeDto outcome) {
        log.error("Event outcome exhausted all retries and landed in DLT — manual intervention required: {}", outcome);
    }
}
