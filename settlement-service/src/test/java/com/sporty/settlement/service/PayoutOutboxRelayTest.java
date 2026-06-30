package com.sporty.settlement.service;

import com.sporty.settlement.model.PayoutOutboxEntry;
import com.sporty.settlement.payment.PaymentGateway;
import com.sporty.settlement.repository.FailedPayoutRepository;
import com.sporty.settlement.repository.PayoutOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutOutboxRelayTest {

    @Mock PayoutOutboxRepository payoutOutboxRepository;
    @Mock FailedPayoutRepository failedPayoutRepository;
    @Mock PaymentGateway paymentGateway;
    @InjectMocks PayoutOutboxRelay relay;

    @Test
    void relay_unsentEntry_callsGatewayAndMarksSentAt() {
        var entry = entry("bet-1");
        when(payoutOutboxRepository.findBySentAtIsNull(any(PageRequest.class)))
                .thenReturn(List.of(entry))
                .thenReturn(List.of());

        relay.relay();

        verify(paymentGateway).payout(entry.getUserId(), entry.getAmount(), "bet-1");
        assertThat(entry.getSentAt()).isNotNull();
    }

    @Test
    void relay_emptyOutbox_gatewayNotCalled() {
        when(payoutOutboxRepository.findBySentAtIsNull(any(PageRequest.class))).thenReturn(List.of());

        relay.relay();

        verify(paymentGateway, never()).payout(any(), any(), any());
    }

    @Test
    void relay_gatewayFails_entryRemainsUnsentForNextAttempt() {
        var entry = entry("bet-2");
        when(payoutOutboxRepository.findBySentAtIsNull(any(PageRequest.class)))
                .thenReturn(List.of(entry))
                .thenReturn(List.of());
        doThrow(new RuntimeException("gateway unavailable")).when(paymentGateway)
                .payout(any(), any(), any());

        relay.relay();

        assertThat(entry.getSentAt()).isNull();
        assertThat(entry.getRetryCount()).isEqualTo(1);
    }

    @Test
    void relay_gatewayFailsMaxRetries_movedToFailedPayouts() {
        var entry = entry("bet-3");
        entry.setRetryCount(2);
        when(payoutOutboxRepository.findBySentAtIsNull(any(PageRequest.class)))
                .thenReturn(List.of(entry))
                .thenReturn(List.of());
        doThrow(new RuntimeException("gateway unavailable")).when(paymentGateway)
                .payout(any(), any(), any());

        relay.relay();

        verify(failedPayoutRepository).save(any());
        verify(payoutOutboxRepository).delete(entry);
    }

    private PayoutOutboxEntry entry(String idempotencyKey) {
        return PayoutOutboxEntry.builder()
                .id(1L).userId(1L).amount(new BigDecimal("50.00"))
                .idempotencyKey(idempotencyKey).createdAt(Instant.now())
                .build();
    }
}
