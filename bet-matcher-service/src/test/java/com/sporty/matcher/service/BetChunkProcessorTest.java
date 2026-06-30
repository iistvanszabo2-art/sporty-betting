package com.sporty.matcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.matcher.dto.BetSettlementDto;
import com.sporty.matcher.dto.EventOutcomeDto;
import com.sporty.matcher.model.Bet;
import com.sporty.matcher.model.BetStatus;
import com.sporty.matcher.model.OutboxEntry;
import com.sporty.matcher.repository.BetRepository;
import com.sporty.matcher.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetChunkProcessorTest {

    @Mock BetRepository betRepository;
    @Mock OutboxRepository outboxRepository;
    @Mock ObjectMapper objectMapper;
    @InjectMocks BetChunkProcessor processor;

    @Test
    void processChunk_winningBet_settlementHasWonTrue() throws Exception {
        var bet = bet(1L, 10L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        processor.processChunk(List.of(bet), outcome(1L, 10L), "corr-1");

        var dto = captureSettlementDto();
        assertThat(dto.isWon()).isTrue();
        assertThat(dto.getBetId()).isEqualTo(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void processChunk_correlationId_storedOnOutboxEntry() throws Exception {
        var bet = bet(1L, 10L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        processor.processChunk(List.of(bet), outcome(1L, 10L), "corr-1");

        ArgumentCaptor<List<OutboxEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(outboxRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void processChunk_losingBet_settlementHasWonFalse() throws Exception {
        var bet = bet(2L, 20L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        processor.processChunk(List.of(bet), outcome(1L, 10L), "corr-1");

        assertThat(captureSettlementDto().isWon()).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void processChunk_multipleBets_allSavedAndStatusUpdated() throws Exception {
        var winBet  = bet(1L, 10L);
        var loseBet = bet(2L, 20L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        processor.processChunk(List.of(winBet, loseBet), outcome(1L, 10L), "corr-1");

        verify(outboxRepository).saveAll(argThat((List<OutboxEntry> entries) -> entries.size() == 2));
        verify(betRepository).updateStatusByIds(List.of(1L, 2L), BetStatus.PENDING, BetStatus.DISPATCHED);
    }

    private BetSettlementDto captureSettlementDto() throws Exception {
        ArgumentCaptor<BetSettlementDto> captor = ArgumentCaptor.forClass(BetSettlementDto.class);
        verify(objectMapper).writeValueAsString(captor.capture());
        return captor.getValue();
    }

    private Bet bet(Long id, Long winnerId) {
        return Bet.builder()
                .id(id).userId(1L).eventId(1L).eventMarketId(101L)
                .eventWinnerId(winnerId).amount(new BigDecimal("50.00"))
                .status(BetStatus.PENDING).build();
    }

    private EventOutcomeDto outcome(Long eventId, Long winnerId) {
        return EventOutcomeDto.builder().eventId(eventId).eventName("Test").eventWinnerId(winnerId).build();
    }
}
