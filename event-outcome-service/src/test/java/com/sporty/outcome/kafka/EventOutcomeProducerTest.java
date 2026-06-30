package com.sporty.outcome.kafka;

import com.sporty.outcome.config.CorrelationFilter;
import com.sporty.outcome.dto.EventOutcomeDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventOutcomeProducerTest {

    @Mock KafkaTemplate<String, EventOutcomeDto> kafkaTemplate;
    @InjectMocks EventOutcomeProducer producer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MDC.put(CorrelationFilter.MDC_KEY, "test-corr-id");

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(EventOutcomeProducer.TOPIC, 0), 0, 0, 0, 0, 0);
        SendResult<String, EventOutcomeDto> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
    }

    @Test
    void publish_sendsToEventOutcomesTopic() throws Exception {
        producer.publish(dto(1L));
        assertThat(capturedRecord().topic()).isEqualTo(EventOutcomeProducer.TOPIC);
    }

    @Test
    void publish_keyContainsEventId() throws Exception {
        producer.publish(dto(42L));
        assertThat(capturedRecord().key()).isEqualTo("42");
    }

    @Test
    void publish_attachesCorrelationIdHeader() throws Exception {
        producer.publish(dto(1L));
        var header = capturedRecord().headers().lastHeader(CorrelationFilter.MDC_KEY);
        assertThat(header).isNotNull();
        assertThat(new String(header.value())).isEqualTo("test-corr-id");
    }

    @SuppressWarnings("unchecked")
    private ProducerRecord<String, EventOutcomeDto> capturedRecord() {
        ArgumentCaptor<ProducerRecord<String, EventOutcomeDto>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        return captor.getValue();
    }

    private EventOutcomeDto dto(Long eventId) {
        return EventOutcomeDto.builder().eventId(eventId).eventName("Test Event").eventWinnerId(10L).build();
    }
}
