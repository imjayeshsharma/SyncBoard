// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * Context needed to validate one candidate transition: the ticket's current
 * status, the status being requested, the status it was blocked from (if any,
 * only meaningful when {@code current == BLOCKED}), whether it currently has
 * an assignee, and the note supplied with the transition request.
 */
public record TransitionContext(
        TicketStatus current,
        TicketStatus target,
        TicketStatus blockedFrom,
        boolean hasAssignee,
        String note) {
}
