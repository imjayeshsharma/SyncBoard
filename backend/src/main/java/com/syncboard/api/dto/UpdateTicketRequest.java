// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.Priority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code UpdateTicketRequest { title?, description?, category?,
 * priority?, assigneeId?, dueAt?, version* } }. All fields optional except {@code version},
 * which is required for the optimistic-locking check (contract §3).
 */
public record UpdateTicketRequest(
        @Size(max = 200) String title,
        String description,
        String category,
        Priority priority,
        UUID assigneeId,
        Instant dueAt,
        @NotNull Long version
) {
}
