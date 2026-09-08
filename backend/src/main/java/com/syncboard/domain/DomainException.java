// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * Base type for runtime exceptions raised by the domain layer that map onto
 * one of the domain-originated error codes in {@link DomainErrorCode}.
 */
public abstract class DomainException extends RuntimeException {

    private final DomainErrorCode code;

    protected DomainException(DomainErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    /** The domain error code this exception represents. */
    public DomainErrorCode code() {
        return code;
    }
}
