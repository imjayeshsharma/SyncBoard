// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable, framework-free view of a user, mirroring the {@code users} table
 * (01-CONTRACT.md §3). Later stages (A2 persistence) map JPA entities onto this
 * shape and vice versa. {@code department} is nullable.
 */
public record UserSnapshot(
        UUID id,
        String googleSubject,
        String email,
        String fullName,
        String department,
        boolean isActive,
        Instant createdAt) {
}
