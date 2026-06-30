package com.sporty.settlement.service;

import com.sporty.settlement.dto.BetSettlementDto;
import com.sporty.settlement.model.PayoutOutboxEntry;
import com.sporty.settlement.model.SettledBet;
import com.sporty.settlement.repository.PayoutOutboxRepository;
import com.sporty.settlement.repository.SettledBetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock SettledBetRepository settledBetRepository;
    @Mock PayoutOutboxRepository payoutOutboxRepository;
    @InjectMocks SettlementService service;

    @Test
    void settle_winningBet_persistsAndEnqueuesOutbox() {
        var dto = dto(1L, true, "50.00");
        when(settledBetRepository.existsByBetId(1L)).thenReturn(false);

        service.settle(dto);

        var betCaptor = ArgumentCaptor.forClass(SettledBet.class);
        verify(settledBetRepository).save(betCaptor.capture());
        assertThat(betCaptor.getValue().getBetId()).isEqualTo(1L);
        assertThat(betCaptor.getValue().isWon()).isTrue();

        var outboxCaptor = ArgumentCaptor.forClass(PayoutOutboxEntry.class);
        verify(payoutOutboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getIdempotencyKey()).isEqualTo("bet-1");
        assertThat(outboxCaptor.getValue().getAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void settle_losingBet_persistsButSkipsOutbox() {
        var dto = dto(2L, false, "30.00");
        when(settledBetRepository.existsByBetId(2L)).thenReturn(false);

        service.settle(dto);

        verify(settledBetRepository).save(any(SettledBet.class));
        verify(payoutOutboxRepository, never()).save(any());
    }

    @Test
    void settle_concurrentDuplicate_constraintViolationTreatedAsDuplicate() {
        var dto = dto(4L, true, "50.00");
        when(settledBetRepository.existsByBetId(4L)).thenReturn(false);
        when(settledBetRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        service.settle(dto);

        verify(payoutOutboxRepository, never()).save(any());
    }

    @Test
    void settle_duplicateBetId_skipsEntireProcessing() {
        when(settledBetRepository.existsByBetId(3L)).thenReturn(true);

        service.settle(dto(3L, true, "100.00"));

        verify(settledBetRepository, never()).save(any());
        verify(payoutOutboxRepository, never()).save(any());
    }

    private BetSettlementDto dto(Long betId, boolean won, String amount) {
        return BetSettlementDto.builder()
                .betId(betId).userId(1L).eventId(1L)
                .won(won).amount(new BigDecimal(amount)).build();
    }
}
