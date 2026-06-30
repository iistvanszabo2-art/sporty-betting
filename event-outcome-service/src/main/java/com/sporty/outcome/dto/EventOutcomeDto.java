package com.sporty.outcome.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOutcomeDto {

    @NotNull
    private Long eventId;

    @NotBlank
    private String eventName;

    @NotNull
    private Long eventWinnerId;
}
