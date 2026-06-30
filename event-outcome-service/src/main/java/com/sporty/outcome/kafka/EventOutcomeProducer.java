package com.sporty.outcome.kafka;

import com.sporty.outcome.config.CorrelationFilter;
import com.sporty.outcome.dto.EventOutcomeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeProducer {

    static final String TOPIC = "event-outcomes";

    private final KafkaTemplate<String, EventOutcomeDto> kafkaTemplate;

    public void publish(EventOutcomeDto outcome) throws ExecutionException, InterruptedException {
        String correlationId = MDC.get(CorrelationFilter.MDC_KEY);

        ProducerRecord<String, EventOutcomeDto> record = new ProducerRecord<>(
                TOPIC,
                null,
                outcome.getEventId().toString(),
                outcome,
                correlationId != null
                        ? List.of(new RecordHeader(CorrelationFilter.MDC_KEY, correlationId.getBytes(StandardCharsets.UTF_8)))
                        : List.of()
        );

        var result = kafkaTemplate.send(record).get();
        log.debug("Event outcome published: eventId={}, offset={}, partition={}",
                outcome.getEventId(),
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().partition());
    }
}
