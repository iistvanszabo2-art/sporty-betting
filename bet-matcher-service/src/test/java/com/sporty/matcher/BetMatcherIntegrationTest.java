package com.sporty.matcher;

import com.sporty.matcher.dto.EventOutcomeDto;
import com.sporty.matcher.model.BetStatus;
import com.sporty.matcher.repository.BetRepository;
import com.sporty.matcher.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import org.springframework.data.domain.Pageable;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"event-outcomes"})
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class BetMatcherIntegrationTest {

    @Autowired BetRepository betRepository;
    @Autowired OutboxRepository outboxRepository;
    @Autowired KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void consumeEventOutcome_matchingBetsAreDispatchedAndOutboxFilled() {
        kafkaTemplate.send("event-outcomes",
                EventOutcomeDto.builder().eventId(1L).eventName("Champions League").eventWinnerId(10L).build());

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            var dispatched = betRepository.findByEventIdAndStatus(1L, BetStatus.DISPATCHED, Pageable.unpaged());
            assertThat(dispatched).hasSize(2);
            assertThat(outboxRepository.count()).isGreaterThanOrEqualTo(2);
        });
    }

    @Test
    void consumeEventOutcome_noPendingBetsForEvent_outboxUnchanged() {
        long outboxSizeBefore = outboxRepository.count();

        kafkaTemplate.send("event-outcomes",
                EventOutcomeDto.builder().eventId(999L).eventName("Unknown Event").eventWinnerId(1L).build());

        // Give the consumer time to process and confirm nothing was written
        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(outboxRepository.count()).isEqualTo(outboxSizeBefore));
    }
}
