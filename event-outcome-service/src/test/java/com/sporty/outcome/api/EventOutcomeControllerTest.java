package com.sporty.outcome.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.outcome.dto.EventOutcomeDto;
import com.sporty.outcome.service.EventOutcomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventOutcomeController.class)
class EventOutcomeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean EventOutcomeService eventOutcomeService;

    private static final String URL = "/api/v1/event-outcomes";

    @Test
    void publish_validBody_returns202() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(eventOutcomeService).recordAndPublish(any(EventOutcomeDto.class));
    }

    @Test
    void publish_unknownEvent_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(eventOutcomeService).recordAndPublish(any());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void publish_duplicateOutcome_returns409() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT))
                .when(eventOutcomeService).recordAndPublish(any());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isConflict());
    }

    @Test
    void publish_nullEventId_returns400() throws Exception {
        var dto = EventOutcomeDto.builder().eventName("Final").eventWinnerId(10L).build();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(eventOutcomeService, never()).recordAndPublish(any());
    }

    @Test
    void publish_blankEventName_returns400() throws Exception {
        var dto = EventOutcomeDto.builder().eventId(1L).eventName("  ").eventWinnerId(10L).build();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(eventOutcomeService, never()).recordAndPublish(any());
    }

    @Test
    void publish_nullEventWinnerId_returns400() throws Exception {
        var dto = EventOutcomeDto.builder().eventId(1L).eventName("Final").build();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(eventOutcomeService, never()).recordAndPublish(any());
    }

    private EventOutcomeDto validDto() {
        return EventOutcomeDto.builder().eventId(1L).eventName("Final").eventWinnerId(10L).build();
    }
}
