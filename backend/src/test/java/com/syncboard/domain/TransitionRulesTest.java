// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers every legal transition in the contract's transition table, the
 * guard rules that ride along with COMPLETED/CANCELLED/BLOCKED, the BLOCKED
 * round-trip rule, and that terminal statuses have no outgoing edges.
 */
class TransitionRulesTest {

    private static TransitionContext ctx(TicketStatus current, TicketStatus target, TicketStatus blockedFrom,
            boolean hasAssignee, String note) {
        return new TransitionContext(current, target, blockedFrom, hasAssignee, note);
    }

    // -- every legal transition --------------------------------------------

    @Test
    void openToTriaging_isAllowed() {
        var result = TransitionRules.check(ctx(TicketStatus.OPEN, TicketStatus.TRIAGING, null, false, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void openToCancelled_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.OPEN, TicketStatus.CANCELLED, null, false, "no longer needed"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void triagingToInProgress_isAllowed() {
        var result = TransitionRules.check(ctx(TicketStatus.TRIAGING, TicketStatus.IN_PROGRESS, null, false, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void triagingToCancelled_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.TRIAGING, TicketStatus.CANCELLED, null, false, "duplicate"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void inProgressToUnderReview_isAllowed() {
        var result = TransitionRules.check(ctx(TicketStatus.IN_PROGRESS, TicketStatus.UNDER_REVIEW, null, false, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void inProgressToBlocked_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.IN_PROGRESS, TicketStatus.BLOCKED, null, false, "waiting on vendor"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
        var allowed = (TransitionCheck.Allowed) result;
        assertEquals(TicketStatus.IN_PROGRESS, allowed.resultingBlockedFromStatus());
    }

    @Test
    void inProgressToCancelled_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED, null, false, "abandoned"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void underReviewToCompleted_isAllowedWithAssignee() {
        var result = TransitionRules.check(ctx(TicketStatus.UNDER_REVIEW, TicketStatus.COMPLETED, null, true, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void underReviewToInProgress_isAllowed() {
        var result = TransitionRules.check(ctx(TicketStatus.UNDER_REVIEW, TicketStatus.IN_PROGRESS, null, false, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void underReviewToBlocked_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.UNDER_REVIEW, TicketStatus.BLOCKED, null, false, "needs more info"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
        var allowed = (TransitionCheck.Allowed) result;
        assertEquals(TicketStatus.UNDER_REVIEW, allowed.resultingBlockedFromStatus());
    }

    @Test
    void underReviewToCancelled_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.UNDER_REVIEW, TicketStatus.CANCELLED, null, false, "rejected"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    @Test
    void blockedToCancelled_isAllowedWithReason() {
        var result = TransitionRules.check(ctx(TicketStatus.BLOCKED, TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS, false, "giving up"));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    // -- a rejected illegal transition ---------------------------------------

    @Test
    void openToInProgress_isRejectedAsIllegal() {
        var result = TransitionRules.check(ctx(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, null, false, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.ILLEGAL_TRANSITION, rejected.code());
    }

    @Test
    void openToCompleted_isRejectedAsIllegal() {
        var result = TransitionRules.check(ctx(TicketStatus.OPEN, TicketStatus.COMPLETED, null, true, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.ILLEGAL_TRANSITION, rejected.code());
    }

    // -- COMPLETED without assignee -----------------------------------------

    @Test
    void completedWithoutAssignee_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.UNDER_REVIEW, TicketStatus.COMPLETED, null, false, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.ASSIGNEE_REQUIRED, rejected.code());
    }

    // -- CANCELLED without reason --------------------------------------------

    @Test
    void cancelledWithoutReason_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.OPEN, TicketStatus.CANCELLED, null, false, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.REASON_REQUIRED, rejected.code());
    }

    @Test
    void cancelledWithBlankReason_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.OPEN, TicketStatus.CANCELLED, null, false, "   "));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.REASON_REQUIRED, rejected.code());
    }

    // -- BLOCKED without reason -----------------------------------------------

    @Test
    void blockedWithoutReason_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.IN_PROGRESS, TicketStatus.BLOCKED, null, false, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.REASON_REQUIRED, rejected.code());
    }

    // -- BLOCKED -> origin round-trip -----------------------------------------

    @Test
    void blockedToOrigin_isAllowedAndClearsBlockedFrom() {
        var result = TransitionRules.check(ctx(TicketStatus.BLOCKED, TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS, false, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
        var allowed = (TransitionCheck.Allowed) result;
        assertNull(allowed.resultingBlockedFromStatus());
    }

    @Test
    void blockedToOriginUnderReview_isAllowed() {
        var result = TransitionRules.check(ctx(TicketStatus.BLOCKED, TicketStatus.UNDER_REVIEW, TicketStatus.UNDER_REVIEW, false, null));
        assertInstanceOf(TransitionCheck.Allowed.class, result);
    }

    // -- BLOCKED -> a different status is rejected -----------------------------

    @Test
    void blockedToDifferentStatus_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.BLOCKED, TicketStatus.UNDER_REVIEW, TicketStatus.IN_PROGRESS, false, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.ILLEGAL_TRANSITION, rejected.code());
    }

    @Test
    void blockedToCompleted_isRejectedEvenIfNotOrigin() {
        var result = TransitionRules.check(ctx(TicketStatus.BLOCKED, TicketStatus.COMPLETED, TicketStatus.IN_PROGRESS, true, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
    }

    // -- terminal states have no outgoing edges --------------------------------

    @Test
    void completed_isTerminal_withNoAllowedTransitions() {
        assertTrue(TicketStatus.COMPLETED.isTerminal());
        assertTrue(TransitionRules.allowedFrom(TicketStatus.COMPLETED, null).isEmpty());
    }

    @Test
    void cancelled_isTerminal_withNoAllowedTransitions() {
        assertTrue(TicketStatus.CANCELLED.isTerminal());
        assertTrue(TransitionRules.allowedFrom(TicketStatus.CANCELLED, null).isEmpty());
    }

    @Test
    void completedToAnything_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.COMPLETED, TicketStatus.OPEN, null, true, "reopen"));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.ILLEGAL_TRANSITION, rejected.code());
    }

    @Test
    void cancelledToAnything_isRejected() {
        var result = TransitionRules.check(ctx(TicketStatus.CANCELLED, TicketStatus.OPEN, null, true, null));
        assertInstanceOf(TransitionCheck.Rejected.class, result);
        var rejected = (TransitionCheck.Rejected) result;
        assertEquals(DomainErrorCode.ILLEGAL_TRANSITION, rejected.code());
    }
}
