// Stage 2 — REST API controllers
package com.syncboard.api.controller;

import com.syncboard.api.dto.CreateLinkRequest;
import com.syncboard.api.dto.TicketLinkDto;
import com.syncboard.service.TicketLinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code /api/v1/tickets/{id}/links}. No logic here —
 * delegates to {@link TicketLinkService}.
 */
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/links")
public class TicketLinkController {

    private final TicketLinkService ticketLinkService;

    public TicketLinkController(TicketLinkService ticketLinkService) {
        this.ticketLinkService = ticketLinkService;
    }

    @GetMapping
    public List<TicketLinkDto> listLinks(@PathVariable UUID ticketId) {
        return ticketLinkService.listLinks(ticketId);
    }

    @PostMapping
    public ResponseEntity<TicketLinkDto> createLink(@PathVariable UUID ticketId, @Valid @RequestBody CreateLinkRequest request) {
        TicketLinkDto created = ticketLinkService.createLink(ticketId, request);
        return ResponseEntity.created(URI.create("/api/v1/tickets/" + ticketId + "/links/" + created.id())).body(created);
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID ticketId, @PathVariable UUID linkId) {
        ticketLinkService.deleteLink(ticketId, linkId);
        return ResponseEntity.noContent().build();
    }
}
