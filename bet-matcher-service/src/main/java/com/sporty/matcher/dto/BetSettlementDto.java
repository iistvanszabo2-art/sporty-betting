package com.sporty.matcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetSettlementDto {

    private Long betId;
    private Long userId;
    private Long eventId;
    private boolean won;
    private BigDecimal amount;
}
