// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Contract 01-CONTRACT.md §4: {@code BoardResponse { columns: BoardColumn[], generatedAt } }.
 * Column order is fixed by {@code TicketStatus.boardColumns()}: Open, Triaging, InProgress,
 * UnderReview, Blocked, Completed. Cancelled is never a column.
 */
public record BoardResponse(
        List<BoardColumn> columns,
        Instant generatedAt
) {
}
