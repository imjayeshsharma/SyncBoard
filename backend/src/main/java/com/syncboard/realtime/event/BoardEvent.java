// Stage 6 — Cache + realtime
package com.syncboard.realtime.event;

import java.time.Instant;
import java.util.UUID;

import com.syncboard.domain.TicketStatus;

/**
 * Payload published to {@code /topic/board} (01-CONTRACT.md §4):
 * {@code { "type", "ticketId", "fromStatus"?, "toStatus"?, "at" }}.
 * {@code fromStatus} is {@code null} for {@link BoardEventType#TICKET_CREATED}.
 * Enum fields serialise to their wire values via A3's {@code DomainEnumModule}, which the
 * shared {@link com.fasterxml.jackson.databind.ObjectMapper} bean applies to this STOMP
 * payload too (see {@code realtime.WebSocketConfig#configureMessageConverters}).
 */
public record BoardEvent(
        BoardEventType type,
        UUID ticketId,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        Instant at) {
}
