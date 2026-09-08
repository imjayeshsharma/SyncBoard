// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable, framework-free view of a ticket link, mirroring the {@code
 * ticket_links} table (01-CONTRACT.md §3). {@code linkTitle} is nullable.
 */
public record LinkSnapshot(
        UUID id,
        UUID ticketId,
        String url,
        String linkTitle,
        LinkPlatform platform,
        Instant createdAt) {
}
