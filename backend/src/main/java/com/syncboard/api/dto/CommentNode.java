// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4:
 * {@code CommentNode { id, bodyText, author: UserSummary, createdAt, editedAt?, replies: CommentNode[] } }.
 * Threaded comments are represented as a tree; {@link #replies()} holds direct children.
 */
public record CommentNode(
        UUID id,
        String bodyText,
        UserSummary author,
        Instant createdAt,
        Instant editedAt,
        List<CommentNode> replies
) {
}
