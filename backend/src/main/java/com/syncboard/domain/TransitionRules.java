// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The legal ticket-status transition table (01-CONTRACT.md §2) plus the guard
 * rules that ride along with certain transitions. This is the single source of
 * truth the API layer (A3) is expected to enforce through {@link #check}.
 */
public final class TransitionRules {

    private static final Map<TicketStatus, Set<TicketStatus>> BASE_TRANSITIONS = baseTransitions();

    private TransitionRules() {
    }

    private static Map<TicketStatus, Set<TicketStatus>> baseTransitions() {
        Map<TicketStatus, Set<TicketStatus>> table = new EnumMap<>(TicketStatus.class);
        table.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.TRIAGING, TicketStatus.CANCELLED));
        table.put(TicketStatus.TRIAGING, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        table.put(TicketStatus.IN_PROGRESS,
                EnumSet.of(TicketStatus.UNDER_REVIEW, TicketStatus.BLOCKED, TicketStatus.CANCELLED));
        table.put(TicketStatus.UNDER_REVIEW, EnumSet.of(TicketStatus.COMPLETED, TicketStatus.IN_PROGRESS,
                TicketStatus.BLOCKED, TicketStatus.CANCELLED));
        // BLOCKED's other legal target (the status it was blocked from) is dynamic;
        // CANCELLED is the only statically-known target from BLOCKED.
        table.put(TicketStatus.BLOCKED, EnumSet.of(TicketStatus.CANCELLED));
        table.put(TicketStatus.COMPLETED, EnumSet.noneOf(TicketStatus.class));
        table.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
        return table;
    }

    /**
     * The full set of statuses legally reachable from {@code current}. When
     * {@code current == BLOCKED}, {@code blockedFrom} (if non-null) is folded in
     * as the round-trip target, per the contract's BLOCKED rule.
     */
    public static Set<TicketStatus> allowedFrom(TicketStatus current, TicketStatus blockedFrom) {
        Set<TicketStatus> base = BASE_TRANSITIONS.get(current);
        if (current != TicketStatus.BLOCKED || blockedFrom == null) {
            return Set.copyOf(base);
        }
        Set<TicketStatus> withRoundTrip = EnumSet.copyOf(base);
        withRoundTrip.add(blockedFrom);
        return Set.copyOf(withRoundTrip);
    }

    /**
     * Validates one candidate transition against the legal-transition table and
     * the accompanying guard rules (assignee required for COMPLETED, reason
     * required for CANCELLED and BLOCKED).
     */
    public static TransitionCheck check(TransitionContext ctx) {
        TicketStatus current = ctx.current();
        TicketStatus target = ctx.target();

        if (!allowedFrom(current, ctx.blockedFrom()).contains(target)) {
            return new TransitionCheck.Rejected(DomainErrorCode.ILLEGAL_TRANSITION,
                    "Cannot transition from " + current.getWireValue() + " to " + target.getWireValue());
        }

        if (target == TicketStatus.COMPLETED && !ctx.hasAssignee()) {
            return new TransitionCheck.Rejected(DomainErrorCode.ASSIGNEE_REQUIRED,
                    "Ticket must have an assignee before it can be completed");
        }

        if (target == TicketStatus.CANCELLED && isBlank(ctx.note())) {
            return new TransitionCheck.Rejected(DomainErrorCode.REASON_REQUIRED,
                    "A note is required when cancelling a ticket");
        }

        if (target == TicketStatus.BLOCKED && isBlank(ctx.note())) {
            return new TransitionCheck.Rejected(DomainErrorCode.REASON_REQUIRED,
                    "A note is required when blocking a ticket");
        }

        TicketStatus resultingBlockedFrom = target == TicketStatus.BLOCKED ? current : null;
        return new TransitionCheck.Allowed(resultingBlockedFrom);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
