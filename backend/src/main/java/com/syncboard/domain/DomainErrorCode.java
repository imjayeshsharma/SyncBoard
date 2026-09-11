// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * Error codes that originate in the domain layer. Mirrors a subset of the
 * error envelope codes defined in 01-CONTRACT.md §4 — the ones the domain
 * itself is responsible for detecting.
 */
public enum DomainErrorCode {
    ILLEGAL_TRANSITION,
    ASSIGNEE_REQUIRED,
    REASON_REQUIRED
}
