package com.sporty.matcher.service;

import com.sporty.matcher.dto.EventOutcomeDto;
import com.sporty.matcher.model.Bet;
import com.sporty.matcher.model.BetStatus;
import com.sporty.matcher.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetMatcherService {

    private static final int CHUNK_SIZE = 500;

    private final BetRepository betRepository;
    private final BetChunkProcessor chunkProcessor;

    public void processEventOutcome(EventOutcomeDto outcome, String correlationId) {
        log.info("Processing event outcome: eventId={}, eventName='{}', winnerId={}",
                outcome.getEventId(), outcome.getEventName(), outcome.getEventWinnerId());

        int totalMatched = 0;

        List<Bet> bets = fetchPendingChunk(outcome.getEventId());
        while (!bets.isEmpty()) {
            chunkProcessor.processChunk(bets, outcome, correlationId);
            totalMatched += bets.size();
            bets = fetchPendingChunk(outcome.getEventId());
        }

        if (totalMatched == 0) {
            log.info("No pending bets found for eventId={}", outcome.getEventId());
        } else {
            log.info("Matched {} total pending bet(s) for eventId={}", totalMatched, outcome.getEventId());
        }
    }

    private List<Bet> fetchPendingChunk(Long eventId) {
        return betRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING, PageRequest.of(0, CHUNK_SIZE))
                .getContent();
    }
}
