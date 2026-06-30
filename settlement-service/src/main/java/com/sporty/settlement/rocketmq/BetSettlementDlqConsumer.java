package com.sporty.settlement.rocketmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.settlement.dto.BetSettlementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "%DLQ%bet-settlement-consumer-group",
        consumerGroup = "bet-settlement-dlq-consumer-group"
)
public class BetSettlementDlqConsumer implements RocketMQListener<MessageExt> {

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MessageExt message) {
        String correlationId = message.getUserProperty("correlationId");
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        try {
            BetSettlementDto settlement = objectMapper.readValue(message.getBody(), BetSettlementDto.class);
            log.error("[DLQ] Unprocessable bet settlement — manual intervention required: {}", settlement);
        } catch (IOException e) {
            log.error("[DLQ] Failed to deserialize dead-letter message", e);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
