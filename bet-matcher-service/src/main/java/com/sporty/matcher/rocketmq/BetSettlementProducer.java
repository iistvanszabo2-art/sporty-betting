package com.sporty.matcher.rocketmq;

import com.sporty.matcher.dto.BetSettlementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.MDC;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetSettlementProducer {

    static final String TOPIC = "bet-settlements";

    private final RocketMQTemplate rocketMQTemplate;

    public void send(BetSettlementDto settlement) {
        log.info("Sending bet settlement to RocketMQ topic '{}': {}", TOPIC, settlement);
        String correlationId = MDC.get("correlationId");
        MessageBuilder<BetSettlementDto> builder = MessageBuilder.withPayload(settlement)
                .setHeader(RocketMQHeaders.KEYS, String.valueOf(settlement.getBetId()));
        if (correlationId != null) {
            builder.setHeader("correlationId", correlationId);
        }
        rocketMQTemplate.send(TOPIC, builder.build());
    }

}
