// Stage 6 — Cache + realtime
package com.syncboard.realtime.event;

/** The {@code type} discriminator of a {@code /topic/board} STOMP message (01-CONTRACT.md §4). */
public enum BoardEventType {
    TICKET_MOVED,
    TICKET_CREATED,
    TICKET_UPDATED
}
