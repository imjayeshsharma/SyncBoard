// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code UserSummary { id, fullName, email, department?, active }}.
 */
public record UserSummary(
        UUID id,
        String fullName,
        String email,
        String department,
        boolean active
) {
}
