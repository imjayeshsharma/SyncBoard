// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * An unconditional {@code from -> to} status pair, independent of guard rules
 * (assignee/reason requirements) or the dynamic BLOCKED round-trip target.
 */
public record StatusTransition(TicketStatus from, TicketStatus to) {
}
