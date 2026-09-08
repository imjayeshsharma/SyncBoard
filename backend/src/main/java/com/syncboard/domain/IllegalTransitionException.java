// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * Raised when a requested ticket status transition is rejected by
 * {@link TransitionRules#check}, whether because the transition itself is
 * illegal or because it fails a guard (missing assignee/reason).
 */
public final class IllegalTransitionException extends DomainException {

    public IllegalTransitionException(DomainErrorCode code, String message) {
        super(code, message);
    }

    /** Builds this exception directly from a rejected transition check. */
    public static IllegalTransitionException from(TransitionCheck.Rejected rejected) {
        return new IllegalTransitionException(rejected.code(), rejected.message());
    }
}
