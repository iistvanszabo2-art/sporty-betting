package com.sporty.outcome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.outcome.config.CorrelationFilter;
import com.sporty.outcome.dto.EventOutcomeDto;
import com.sporty.outcome.model.Event;
import com.sporty.outcome.model.OutboxEntry;
import com.sporty.outcome.repository.EventRepository;
import com.sporty.outcome.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOutcomeService {

    private final EventRepository eventRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void recordAndPublish(EventOutcomeDto dto) {
        Event event = eventRepository.findByEventId(dto.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found: " + dto.getEventId()));

        if (event.getEventWinnerId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outcome already recorded for event: " + dto.getEventId());
        }

        event.setEventWinnerId(dto.getEventWinnerId());
        event.setRecordedAt(LocalDateTime.now());

        // not relying on broker availability
        outboxRepository.save(OutboxEntry.builder()
                .eventId(dto.getEventId())
                .payload(toJson(dto))
                .correlationId(MDC.get(CorrelationFilter.MDC_KEY))
                .createdAt(Instant.now())
                .build());

        log.info("Event outcome recorded and queued in outbox: eventId={}, winnerId={}",
                dto.getEventId(), dto.getEventWinnerId());
    }

    private String toJson(EventOutcomeDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize EventOutcomeDto for eventId=" + dto.getEventId(), e);
        }
    }
}
