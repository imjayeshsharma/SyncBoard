// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Contract 01-CONTRACT.md §4: {@code TransitionRequest { toStatus*, note?, version* } }.
 * {@code note} is conditionally required by domain rules (REASON_REQUIRED / BLOCKED) —
 * enforced by {@code TransitionRules.check}, not by bean validation here.
 */
public record TransitionRequest(
        @NotNull TicketStatus toStatus,
        String note,
        @NotNull Long version
) {
}
