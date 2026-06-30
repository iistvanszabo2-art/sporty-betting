package com.sporty.outcome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.outcome.dto.EventOutcomeDto;
import com.sporty.outcome.kafka.EventOutcomeProducer;
import com.sporty.outcome.model.OutboxEntry;
import com.sporty.outcome.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock OutboxRepository outboxRepository;
    @Mock EventOutcomeProducer producer;
    @Mock ObjectMapper objectMapper;
    @InjectMocks OutboxRelay relay;

    @Test
    void relay_unsentEntry_publishesAndMarksSentAt() throws Exception {
        var entry = OutboxEntry.builder().id(1L).eventId(1L).payload("{}").build();
        var dto = EventOutcomeDto.builder().eventId(1L).eventName("Final").eventWinnerId(10L).build();
        when(outboxRepository.findBySentAtIsNull(any(PageRequest.class)))
                .thenReturn(List.of(entry))
                .thenReturn(List.of());
        when(objectMapper.readValue("{}", EventOutcomeDto.class)).thenReturn(dto);

        relay.relay();

        verify(producer).publish(dto);
        assertThat(entry.getSentAt()).isNotNull();
    }

    @Test
    void relay_emptyOutbox_producerNotCalled() throws Exception {
        when(outboxRepository.findBySentAtIsNull(any(PageRequest.class))).thenReturn(List.of());

        relay.relay();

        verify(producer, never()).publish(any());
    }

    @Test
    void relay_publishFails_entryRemainsUnsentForNextAttempt() throws Exception {
        var entry = OutboxEntry.builder().id(1L).eventId(1L).payload("{}").build();
        var dto = EventOutcomeDto.builder().eventId(1L).eventName("Final").eventWinnerId(10L).build();
        when(outboxRepository.findBySentAtIsNull(any(PageRequest.class)))
                .thenReturn(List.of(entry))
                .thenReturn(List.of());
        when(objectMapper.readValue("{}", EventOutcomeDto.class)).thenReturn(dto);
        doThrow(new RuntimeException("broker unavailable")).when(producer).publish(dto);

        relay.relay();

        assertThat(entry.getSentAt()).isNull();
    }
}
