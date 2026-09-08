// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code TicketDetail { ...all TicketSummary fields..., description?,
 * blockedFromStatus?, closedAt?, links: TicketLink[], comments: CommentNode[], history: StatusHistoryEntry[] } }.
 * Java records cannot extend one another, so every {@link TicketSummary} field is repeated here
 * verbatim alongside the detail-only fields.
 */
public record TicketDetail(
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
        long version,
        String description,
        TicketStatus blockedFromStatus,
        Instant closedAt,
        List<TicketLinkDto> links,
        List<CommentNode> comments,
        List<StatusHistoryEntry> history
) {
}
