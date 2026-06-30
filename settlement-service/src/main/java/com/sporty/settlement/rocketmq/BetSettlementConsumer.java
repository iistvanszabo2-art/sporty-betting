package com.sporty.settlement.rocketmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.settlement.dto.BetSettlementDto;
import com.sporty.settlement.service.SettlementService;
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
        topic = "bet-settlements",
        consumerGroup = "bet-settlement-consumer-group",
        maxReconsumeTimes = 3
)
public class BetSettlementConsumer implements RocketMQListener<MessageExt> {

    private final SettlementService settlementService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MessageExt message) {
        String correlationId = message.getUserProperty("correlationId");
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }
        try {
            BetSettlementDto settlement = objectMapper.readValue(message.getBody(), BetSettlementDto.class);
            log.info("Received bet settlement from RocketMQ: {}", settlement);
            settlementService.settle(settlement);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize BetSettlementDto", e);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
