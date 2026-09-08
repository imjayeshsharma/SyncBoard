// Stage 2 — REST API controllers
package com.syncboard.api.controller;

import com.syncboard.api.dto.CreateTicketRequest;
import com.syncboard.api.dto.PageResponse;
import com.syncboard.api.dto.StatusHistoryEntry;
import com.syncboard.api.dto.TicketDetail;
import com.syncboard.api.dto.TicketSummary;
import com.syncboard.api.dto.TransitionRequest;
import com.syncboard.api.dto.UpdateTicketRequest;
import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;
import com.syncboard.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4 — every {@code /api/v1/tickets...} endpoint except the nested
 * comments and links resources (own controllers). No logic here — delegates to {@link TicketService}.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public PageResponse<TicketSummary> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ticketService.listTickets(status, assigneeId, priority, q, page, size);
    }

    @PostMapping
    public ResponseEntity<TicketDetail> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDetail created = ticketService.createTicket(request);
        return ResponseEntity.created(URI.create("/api/v1/tickets/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public TicketDetail getTicket(@PathVariable UUID id) {
        return ticketService.getTicket(id);
    }

    @PatchMapping("/{id}")
    public TicketDetail updateTicket(@PathVariable UUID id, @Valid @RequestBody UpdateTicketRequest request) {
        return ticketService.updateTicket(id, request);
    }

    @PostMapping("/{id}/transitions")
    public TicketDetail transition(@PathVariable UUID id, @Valid @RequestBody TransitionRequest request) {
        return ticketService.transition(id, request);
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryEntry> getHistory(@PathVariable UUID id) {
        return ticketService.getHistory(id);
    }
}
