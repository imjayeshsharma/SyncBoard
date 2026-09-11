// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.TicketStatus;

import java.util.List;

/**
 * Contract 01-CONTRACT.md §4: {@code BoardColumn { status, label, tickets: TicketSummary[] } }.
 * {@code label} is the human board-column label from contract §2 (e.g. "In Progress").
 */
public record BoardColumn(
        TicketStatus status,
        String label,
        List<TicketSummary> tickets
) {
}
