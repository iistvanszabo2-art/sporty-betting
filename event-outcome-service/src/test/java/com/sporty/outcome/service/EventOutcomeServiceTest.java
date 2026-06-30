package com.sporty.outcome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.outcome.dto.EventOutcomeDto;
import com.sporty.outcome.model.Event;
import com.sporty.outcome.repository.EventRepository;
import com.sporty.outcome.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventOutcomeServiceTest {

    @Mock EventRepository eventRepository;
    @Mock OutboxRepository outboxRepository;
    @Mock ObjectMapper objectMapper;
    @InjectMocks EventOutcomeService service;

    @Test
    void recordAndPublish_knownEvent_savesOutcomeAndOutboxEntry() throws Exception {
        when(eventRepository.findByEventId(1L)).thenReturn(Optional.of(event(1L, null)));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        service.recordAndPublish(dto(1L));

        verify(outboxRepository).save(any());
    }

    @Test
    void recordAndPublish_unknownEvent_throws404() {
        when(eventRepository.findByEventId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordAndPublish(dto(99L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        verify(outboxRepository, never()).save(any());
    }

    @Test
    void recordAndPublish_duplicateOutcome_throws409() {
        when(eventRepository.findByEventId(1L)).thenReturn(Optional.of(event(1L, 10L)));

        assertThatThrownBy(() -> service.recordAndPublish(dto(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        verify(outboxRepository, never()).save(any());
    }

    private EventOutcomeDto dto(Long eventId) {
        return EventOutcomeDto.builder().eventId(eventId).eventName("Test").eventWinnerId(10L).build();
    }

    private Event event(Long eventId, Long winnerId) {
        return Event.builder().eventId(eventId).name("Test").eventWinnerId(winnerId).build();
    }
}
