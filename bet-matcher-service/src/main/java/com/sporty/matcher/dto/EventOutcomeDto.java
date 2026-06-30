package com.sporty.matcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOutcomeDto {

    private Long eventId;
    private String eventName;
    private Long eventWinnerId;
}
