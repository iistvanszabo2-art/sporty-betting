package com.sporty.settlement;

import com.sporty.settlement.dto.BetSettlementDto;
import com.sporty.settlement.repository.SettledBetRepository;
import com.sporty.settlement.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
class SettlementIntegrationTest {

    @Autowired SettlementService settlementService;
    @Autowired SettledBetRepository repository;

    @Test
    void settle_winningBet_persistedToH2() {
        settlementService.settle(dto(100L, true, "75.00"));

        var saved = repository.findAll().stream()
                .filter(b -> b.getBetId().equals(100L)).findFirst();
        assertThat(saved).isPresent();
        assertThat(saved.get().isWon()).isTrue();
        assertThat(saved.get().getAmount()).isEqualByComparingTo("75.00");
        assertThat(saved.get().getSettledAt()).isNotNull();
    }

    @Test
    void settle_losingBet_persistedToH2() {
        settlementService.settle(dto(101L, false, "30.00"));

        var saved = repository.findAll().stream()
                .filter(b -> b.getBetId().equals(101L)).findFirst();
        assertThat(saved).isPresent();
        assertThat(saved.get().isWon()).isFalse();
    }

    @Test
    void settle_duplicateMessage_idempotentNoDuplicate() {
        settlementService.settle(dto(102L, true, "50.00"));
        settlementService.settle(dto(102L, true, "50.00"));

        long count = repository.findAll().stream()
                .filter(b -> b.getBetId().equals(102L)).count();
        assertThat(count).isEqualTo(1);
    }

    private BetSettlementDto dto(Long betId, boolean won, String amount) {
        return BetSettlementDto.builder()
                .betId(betId).userId(1L).eventId(1L)
                .won(won).amount(new BigDecimal(amount))
                .build();
    }
}
