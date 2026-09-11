// Stage 2 — REST API tests
package com.syncboard.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syncboard.api.dto.CreateTicketRequest;
import com.syncboard.api.dto.TicketDetail;
import com.syncboard.api.dto.TransitionRequest;
import com.syncboard.api.dto.UserSummary;
import com.syncboard.api.jackson.JacksonConfig;
import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;
import com.syncboard.service.TicketService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for {@link TicketController}: JSON shape of {@link TicketDetail},
 * the 201 + Location on create, and the 409 envelope on a version conflict.
 * {@link TicketService} is mocked — no persistence or domain rule engine involved.
 */
@WebMvcTest(TicketController.class)
@Import(JacksonConfig.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Test
    void getTicket_returnsTicketDetailJsonShape() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketDetail detail = sampleDetail(ticketId);
        when(ticketService.getTicket(ticketId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Sample ticket"))
                .andExpect(jsonPath("$.status").value("Open"))
                .andExpect(jsonPath("$.priority").value("High"))
                .andExpect(jsonPath("$.reporter.email").value("reporter@example.com"))
                .andExpect(jsonPath("$.overdue").value(false))
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.history").isArray())
                // null-omission (package-info @JsonInclude(NON_NULL)): no assignee was set.
                .andExpect(jsonPath("$.assignee").doesNotExist());
    }

    @Test
    void createTicket_returns201WithLocationHeader() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketDetail detail = sampleDetail(ticketId);
        when(ticketService.createTicket(any())).thenReturn(detail);

        CreateTicketRequest request = new CreateTicketRequest(
                "Sample ticket", "Description", "Infra", Priority.HIGH, null, null, null
        );

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tickets/" + ticketId))
                .andExpect(jsonPath("$.id").value(ticketId.toString()));
    }

    @Test
    void transition_versionConflict_returnsVersionConflictEnvelope() throws Exception {
        UUID ticketId = UUID.randomUUID();
        when(ticketService.transition(eq(ticketId), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("com.syncboard.persistence.entity.TicketEntity", ticketId));

        TransitionRequest request = new TransitionRequest(TicketStatus.TRIAGING, null, 0L);

        mockMvc.perform(post("/api/v1/tickets/{id}/transitions", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"))
                .andExpect(jsonPath("$.path").value("/api/v1/tickets/" + ticketId + "/transitions"));
    }

    @Test
    @Disabled("TODO stage 2: listTickets filtering/pagination not yet implemented")
    void listTickets_appliesFiltersAndPaging() {
    }

    @Test
    @Disabled("TODO stage 2: updateTicket partial-update semantics")
    void updateTicket_appliesPartialFields() {
    }

    @Test
    @Disabled("TODO stage 2: getHistory endpoint")
    void getHistory_returnsStatusHistoryEntries() {
    }

    private TicketDetail sampleDetail(UUID ticketId) {
        UserSummary reporter = new UserSummary(UUID.randomUUID(), "Reporter Name", "reporter@example.com", null, true);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new TicketDetail(
                ticketId,
                "Sample ticket",
                TicketStatus.OPEN,
                Priority.HIGH,
                "Infra",
                null,
                reporter,
                null,
                false,
                0,
                0,
                now,
                now,
                0L,
                "Description",
                null,
                null,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
