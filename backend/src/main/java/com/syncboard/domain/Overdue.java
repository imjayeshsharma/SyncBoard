// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.time.Instant;

/**
 * The single source of truth for the derived "overdue" rule (01-CONTRACT.md
 * §3). {@code overdue} is never persisted — it is always computed at read time.
 */
public final class Overdue {

    private Overdue() {
    }

    /**
     * A ticket is overdue when it has a due date in the past and its status is
     * not one of the terminal statuses (COMPLETED, CANCELLED). A null due date
     * is never overdue.
     */
    public static boolean isOverdue(Instant dueAt, TicketStatus status, Instant now) {
        if (dueAt == null) {
            return false;
        }
        return dueAt.isBefore(now) && status.isActive();
    }
}
