// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable, framework-free view of a ticket, mirroring the {@code tickets}
 * table (01-CONTRACT.md §3). Later stages (A2 persistence, A3 api+service) map
 * JPA entities and API DTOs onto this shape. {@code description}, {@code
 * category}, {@code assigneeId}, {@code blockedFromStatus}, {@code dueAt} and
 * {@code closedAt} are nullable. {@code overdue} is never stored — see
 * {@link Overdue}.
 */
public record TicketSnapshot(
        UUID id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        String category,
        UUID reporterId,
        UUID assigneeId,
        TicketStatus blockedFromStatus,
        Instant dueAt,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt,
        long version) {
}
