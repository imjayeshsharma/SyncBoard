// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable, framework-free view of a (possibly threaded) comment, mirroring
 * the {@code comments} table (01-CONTRACT.md §3). {@code parentCommentId} and
 * {@code editedAt} are nullable.
 */
public record CommentSnapshot(
        UUID id,
        UUID ticketId,
        UUID authorId,
        String bodyText,
        UUID parentCommentId,
        Instant createdAt,
        Instant editedAt) {
}
