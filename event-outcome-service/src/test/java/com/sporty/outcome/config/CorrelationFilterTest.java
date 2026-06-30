package com.sporty.outcome.config;

import com.sporty.outcome.api.EventOutcomeController;
import com.sporty.outcome.service.EventOutcomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(EventOutcomeController.class)
class CorrelationFilterTest {

    @Autowired MockMvc mockMvc;
    @MockBean EventOutcomeService eventOutcomeService;

    private static final String BODY = "{\"eventId\":1,\"eventName\":\"Test\",\"eventWinnerId\":10}";
    private static final String UUID_PATTERN =
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Test
    void withCorrelationIdHeader_echoesItInResponse() throws Exception {
        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CorrelationFilter.HEADER, "my-trace-id")
                        .content(BODY))
                .andExpect(header().string(CorrelationFilter.HEADER, "my-trace-id"));
    }

    @Test
    void withoutCorrelationIdHeader_generatesUuidAndSetsResponseHeader() throws Exception {
        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(header().string(CorrelationFilter.HEADER, matchesPattern(UUID_PATTERN)));
    }
}
