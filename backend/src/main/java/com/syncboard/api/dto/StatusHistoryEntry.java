// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4:
 * {@code StatusHistoryEntry { id, fromStatus?, toStatus, changedBy: UserSummary, changedAt, note? } }.
 */
public record StatusHistoryEntry(
        UUID id,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        UserSummary changedBy,
        Instant changedAt,
        String note
) {
}
