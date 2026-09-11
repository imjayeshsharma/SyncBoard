// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.util.UUID;

/** Raised when a ticket lookup by id finds no matching ticket. */
public final class TicketNotFoundException extends RuntimeException {

    private final UUID ticketId;

    public TicketNotFoundException(UUID ticketId) {
        super("Ticket not found: " + ticketId);
        this.ticketId = ticketId;
    }

    /** The id that could not be resolved to a ticket. */
    public UUID ticketId() {
        return ticketId;
    }
}
