package com.sporty.outcome.api;

import com.sporty.outcome.api.generated.EventOutcomeApi;
import com.sporty.outcome.api.generated.model.EventOutcomeResponse;
import com.sporty.outcome.dto.EventOutcomeDto;
import com.sporty.outcome.service.EventOutcomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventOutcomeController implements EventOutcomeApi {

    private final EventOutcomeService eventOutcomeService;

    @Override
    public ResponseEntity<EventOutcomeResponse> recordEventOutcome(EventOutcomeDto outcome) {
        eventOutcomeService.recordAndPublish(outcome);
        return ResponseEntity.accepted().body(
                new EventOutcomeResponse()
                        .eventId(outcome.getEventId())
                        .status("ACCEPTED")
        );
    }

}
