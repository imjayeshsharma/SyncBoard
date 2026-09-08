// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.util.UUID;

/** Raised when a user lookup by id finds no matching user. */
public final class UserNotFoundException extends RuntimeException {

    private final UUID userId;

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
        this.userId = userId;
    }

    /** The id that could not be resolved to a user. */
    public UUID userId() {
        return userId;
    }
}
