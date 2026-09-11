// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.LinkPlatform;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code TicketLink { id, url, linkTitle?, platform, createdAt }}.
 * Named {@code TicketLinkDto} (not {@code TicketLink}) to avoid a name clash with the
 * persistence entity of the same concept.
 */
public record TicketLinkDto(
        UUID id,
        String url,
        String linkTitle,
        LinkPlatform platform,
        Instant createdAt
) {
}
