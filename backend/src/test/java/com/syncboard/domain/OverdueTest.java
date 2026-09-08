// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the derived-overdue rule: past/future/null due dates, and that
 * terminal statuses (COMPLETED, CANCELLED) suppress overdue even when the
 * due date has passed.
 */
class OverdueTest {

    private static final Instant NOW = Instant.parse("2026-09-08T12:00:00Z");

    @Test
    void pastDueDate_activeStatus_isOverdue() {
        Instant dueAt = NOW.minus(1, ChronoUnit.DAYS);
        assertTrue(Overdue.isOverdue(dueAt, TicketStatus.IN_PROGRESS, NOW));
    }

    @Test
    void futureDueDate_activeStatus_isNotOverdue() {
        Instant dueAt = NOW.plus(1, ChronoUnit.DAYS);
        assertFalse(Overdue.isOverdue(dueAt, TicketStatus.IN_PROGRESS, NOW));
    }

    @Test
    void nullDueDate_isNeverOverdue() {
        assertFalse(Overdue.isOverdue(null, TicketStatus.IN_PROGRESS, NOW));
    }

    @Test
    void pastDueDate_completedStatus_isNotOverdue() {
        Instant dueAt = NOW.minus(1, ChronoUnit.DAYS);
        assertFalse(Overdue.isOverdue(dueAt, TicketStatus.COMPLETED, NOW));
    }

    @Test
    void pastDueDate_cancelledStatus_isNotOverdue() {
        Instant dueAt = NOW.minus(1, ChronoUnit.DAYS);
        assertFalse(Overdue.isOverdue(dueAt, TicketStatus.CANCELLED, NOW));
    }

    @Test
    void dueExactlyNow_isNotOverdue() {
        assertFalse(Overdue.isOverdue(NOW, TicketStatus.OPEN, NOW));
    }

    @Test
    void pastDueDate_everyActiveStatus_isOverdue() {
        Instant dueAt = NOW.minus(1, ChronoUnit.DAYS);
        for (TicketStatus status : TicketStatus.values()) {
            if (status.isActive()) {
                assertTrue(Overdue.isOverdue(dueAt, status, NOW), "expected overdue for " + status);
            } else {
                assertFalse(Overdue.isOverdue(dueAt, status, NOW), "expected not overdue for " + status);
            }
        }
    }
}
