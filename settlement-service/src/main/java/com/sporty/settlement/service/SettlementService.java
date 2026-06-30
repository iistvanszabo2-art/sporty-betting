package com.sporty.settlement.service;

import com.sporty.settlement.dto.BetSettlementDto;
import com.sporty.settlement.model.PayoutOutboxEntry;
import com.sporty.settlement.model.SettledBet;
import com.sporty.settlement.repository.PayoutOutboxRepository;
import com.sporty.settlement.repository.SettledBetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettledBetRepository settledBetRepository;
    private final PayoutOutboxRepository payoutOutboxRepository;

    @Transactional
    public void settle(BetSettlementDto dto) {
        if (settledBetRepository.existsByBetId(dto.getBetId())) {
            log.warn("Duplicate settlement message ignored — betId={} already settled", dto.getBetId());
            return;
        }

        try {
            settledBetRepository.save(SettledBet.builder()
                    .betId(dto.getBetId())
                    .userId(dto.getUserId())
                    .eventId(dto.getEventId())
                    .won(dto.isWon())
                    .amount(dto.getAmount())
                    .settledAt(LocalDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate settlement ignored — betId={} already settled", dto.getBetId());
            return;
        }

        if (dto.isWon()) {
            // not calling the payment gateway directly in order not to block the transaction
            payoutOutboxRepository.save(PayoutOutboxEntry.builder()
                    .userId(dto.getUserId())
                    .amount(dto.getAmount())
                    .idempotencyKey("bet-" + dto.getBetId())
                    .createdAt(Instant.now())
                    .build());
        }

        log.info("Bet settled: betId={}, userId={}, won={}, amount={}",
                dto.getBetId(), dto.getUserId(), dto.isWon(), dto.getAmount());
    }
}
