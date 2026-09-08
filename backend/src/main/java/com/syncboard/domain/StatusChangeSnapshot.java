// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable, framework-free view of one append-only status-history row,
 * mirroring the {@code ticket_status_history} table (01-CONTRACT.md §3).
 * {@code fromStatus} and {@code note} are nullable ({@code fromStatus} is null
 * for the ticket's initial creation entry).
 */
public record StatusChangeSnapshot(
        UUID id,
        UUID ticketId,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        UUID changedBy,
        Instant changedAt,
        String note) {
}
