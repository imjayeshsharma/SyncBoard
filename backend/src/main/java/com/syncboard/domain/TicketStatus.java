// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.util.List;

/**
 * The lifecycle status of a {@code Ticket}. Constant names and wire values are
 * fixed by the shared contract (01-CONTRACT.md §2) — do not rename or add synonyms.
 */
public enum TicketStatus {
    OPEN("Open"),
    TRIAGING("Triaging"),
    IN_PROGRESS("InProgress"),
    BLOCKED("Blocked"),
    UNDER_REVIEW("UnderReview"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String wireValue;

    TicketStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    /** The JSON/DB/TS wire value for this status. */
    public String getWireValue() {
        return wireValue;
    }

    /** Resolves a status from its wire value; throws if unknown. */
    public static TicketStatus fromWireValue(String wireValue) {
        for (TicketStatus status : values()) {
            if (status.wireValue.equals(wireValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TicketStatus wire value: " + wireValue);
    }

    /** True for statuses with no legal outgoing transitions (COMPLETED, CANCELLED). */
    public boolean isTerminal() {
        return switch (this) {
            case COMPLETED, CANCELLED -> true;
            case OPEN, TRIAGING, IN_PROGRESS, BLOCKED, UNDER_REVIEW -> false;
        };
    }

    /** True for non-terminal statuses, i.e. the ticket is still in flight. */
    public boolean isActive() {
        return !isTerminal();
    }

    /** True if this status appears as a board column (all but CANCELLED). */
    public boolean isBoardColumn() {
        return switch (this) {
            case CANCELLED -> false;
            case OPEN, TRIAGING, IN_PROGRESS, BLOCKED, UNDER_REVIEW, COMPLETED -> true;
        };
    }

    /** Board columns in the fixed left-to-right display order. */
    public static List<TicketStatus> boardColumns() {
        return List.of(OPEN, TRIAGING, IN_PROGRESS, UNDER_REVIEW, BLOCKED, COMPLETED);
    }
}
