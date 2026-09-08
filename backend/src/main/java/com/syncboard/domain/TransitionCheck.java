// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * The outcome of validating a transition: either {@link Allowed}, carrying the
 * {@code blockedFromStatus} the ticket should be persisted with afterwards, or
 * {@link Rejected}, carrying the domain error code that explains why not.
 */
public sealed interface TransitionCheck permits TransitionCheck.Allowed, TransitionCheck.Rejected {

    /**
     * The transition is legal. {@code resultingBlockedFromStatus} is the value
     * {@code Ticket.blockedFromStatus} should hold after applying it: the
     * pre-transition {@code current} status when moving into BLOCKED, or
     * {@code null} in every other case (including moving out of BLOCKED).
     */
    record Allowed(TicketStatus resultingBlockedFromStatus) implements TransitionCheck {
    }

    /** The transition is illegal or fails a guard; {@code code} says which. */
    record Rejected(DomainErrorCode code, String message) implements TransitionCheck {
    }
}
