// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code CreateCommentRequest { bodyText*, parentCommentId? } }.
 */
public record CreateCommentRequest(
        @NotBlank String bodyText,
        UUID parentCommentId
) {
}
