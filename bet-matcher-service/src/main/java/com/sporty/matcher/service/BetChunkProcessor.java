package com.sporty.matcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.matcher.dto.BetSettlementDto;
import com.sporty.matcher.dto.EventOutcomeDto;
import com.sporty.matcher.model.Bet;
import com.sporty.matcher.model.BetStatus;
import com.sporty.matcher.model.OutboxEntry;
import com.sporty.matcher.repository.BetRepository;
import com.sporty.matcher.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetChunkProcessor {

    private final BetRepository betRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processChunk(List<Bet> bets, EventOutcomeDto outcome, String correlationId) {
        Instant now = Instant.now();
        List<OutboxEntry> outboxEntries = bets.stream()
                .map(bet -> OutboxEntry.builder()
                        .betId(bet.getId())
                        .payload(toJson(buildSettlement(bet, outcome)))
                        .correlationId(correlationId)
                        .createdAt(now)
                        .build())
                .toList();

        // not relying on broker availability
        outboxRepository.saveAll(outboxEntries);

        List<Long> ids = bets.stream().map(Bet::getId).toList();
        betRepository.updateStatusByIds(ids, BetStatus.PENDING, BetStatus.DISPATCHED);

        log.info("Dispatched chunk of {} bets for eventId={}", bets.size(), outcome.getEventId());
    }

    private BetSettlementDto buildSettlement(Bet bet, EventOutcomeDto outcome) {
        return BetSettlementDto.builder()
                .betId(bet.getId())
                .userId(bet.getUserId())
                .eventId(bet.getEventId())
                .won(bet.getEventWinnerId().equals(outcome.getEventWinnerId()))
                .amount(bet.getAmount())
                .build();
    }

    private String toJson(BetSettlementDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize BetSettlementDto for betId=" + dto.getBetId(), e);
        }
    }
}
