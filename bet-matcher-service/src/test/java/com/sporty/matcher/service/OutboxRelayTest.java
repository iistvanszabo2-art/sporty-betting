package com.sporty.matcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.matcher.dto.BetSettlementDto;
import com.sporty.matcher.model.OutboxEntry;
import com.sporty.matcher.repository.OutboxRepository;
import com.sporty.matcher.rocketmq.BetSettlementProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock OutboxRepository outboxRepository;
    @Mock BetSettlementProducer betSettlementProducer;
    @Mock ObjectMapper objectMapper;
    @InjectMocks OutboxRelay relay;

    @Test
    void relay_unsentEntry_sendsItAndMarksSentAt() throws Exception {
        var entry = OutboxEntry.builder().id(1L).betId(10L).payload("{}").build();
        var dto = BetSettlementDto.builder().betId(10L).build();
        when(outboxRepository.findBySentAtIsNull(any(Pageable.class)))
                .thenReturn(List.of(entry)).thenReturn(List.of());
        when(objectMapper.readValue("{}", BetSettlementDto.class)).thenReturn(dto);

        relay.relay();

        verify(betSettlementProducer).send(dto);
        assertThat(entry.getSentAt()).isNotNull();
    }

    @Test
    void relay_emptyOutbox_producerNotCalled() {
        when(outboxRepository.findBySentAtIsNull(any(Pageable.class))).thenReturn(List.of());

        relay.relay();

        verify(betSettlementProducer, never()).send(any());
    }

    @Test
    void relay_producerThrows_entryRemainsUnsentForNextAttempt() throws Exception {
        var entry = OutboxEntry.builder().id(1L).betId(10L).payload("{}").build();
        var dto = BetSettlementDto.builder().betId(10L).build();
        when(outboxRepository.findBySentAtIsNull(any(Pageable.class)))
                .thenReturn(List.of(entry)).thenReturn(List.of());
        when(objectMapper.readValue("{}", BetSettlementDto.class)).thenReturn(dto);
        doThrow(new RuntimeException("broker unavailable")).when(betSettlementProducer).send(dto);

        relay.relay();

        assertThat(entry.getSentAt()).isNull();
    }
}
