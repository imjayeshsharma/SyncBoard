// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code TicketSummary { id, title, status, priority, category?,
 * assignee?: UserSummary, reporter: UserSummary, dueAt?, overdue, commentCount, linkCount,
 * createdAt, updatedAt, version } }.
 * {@code overdue} is always derived server-side, never persisted (see {@code Overdue}).
 */
public record TicketSummary(
        UUID id,
        String title,
        TicketStatus status,
        Priority priority,
        String category,
        UserSummary assignee,
        UserSummary reporter,
        Instant dueAt,
        boolean overdue,
        int commentCount,
        int linkCount,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
