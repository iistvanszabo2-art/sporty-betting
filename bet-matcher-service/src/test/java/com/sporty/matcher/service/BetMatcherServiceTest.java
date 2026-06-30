package com.sporty.matcher.service;

import com.sporty.matcher.dto.EventOutcomeDto;
import com.sporty.matcher.model.Bet;
import com.sporty.matcher.model.BetStatus;
import com.sporty.matcher.repository.BetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetMatcherServiceTest {

    @Mock BetRepository betRepository;
    @Mock BetChunkProcessor chunkProcessor;
    @InjectMocks BetMatcherService service;

    @Test
    void processEventOutcome_noPendingBets_chunkProcessorNotCalled() {
        when(betRepository.findByEventIdAndStatus(eq(99L), eq(BetStatus.PENDING), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.processEventOutcome(outcome(99L, 10L), "corr-1");

        verifyNoInteractions(chunkProcessor);
    }

    @Test
    void processEventOutcome_twoBets_chunkProcessorCalledOnce() {
        var bets = List.of(bet(1L), bet(2L));
        when(betRepository.findByEventIdAndStatus(eq(1L), eq(BetStatus.PENDING), any(Pageable.class)))
                .thenReturn(pageOf(bets), Page.empty());

        service.processEventOutcome(outcome(1L, 10L), "corr-1");

        verify(chunkProcessor).processChunk(bets, outcome(1L, 10L), "corr-1");
    }

    @Test
    void processEventOutcome_twoChunks_chunkProcessorCalledTwice() {
        var firstChunk  = List.of(bet(1L), bet(2L));
        var secondChunk = List.of(bet(3L));
        when(betRepository.findByEventIdAndStatus(eq(1L), eq(BetStatus.PENDING), any(Pageable.class)))
                .thenReturn(pageOf(firstChunk), pageOf(secondChunk), Page.empty());

        service.processEventOutcome(outcome(1L, 10L), "corr-1");

        verify(chunkProcessor, times(2)).processChunk(any(), any(), any());
    }

    @SafeVarargs
    private <T> Page<T> pageOf(T... items) {
        return new PageImpl<>(Arrays.asList(items));
    }

    private Page<Bet> pageOf(List<Bet> bets) {
        return new PageImpl<>(bets);
    }

    private Bet bet(Long id) {
        return Bet.builder()
                .id(id).userId(1L).eventId(1L).eventMarketId(101L)
                .eventWinnerId(10L).amount(new BigDecimal("50.00"))
                .status(BetStatus.PENDING).build();
    }

    private EventOutcomeDto outcome(Long eventId, Long winnerId) {
        return EventOutcomeDto.builder().eventId(eventId).eventName("Test").eventWinnerId(winnerId).build();
    }
}
