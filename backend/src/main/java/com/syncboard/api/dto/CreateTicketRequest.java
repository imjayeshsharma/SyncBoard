// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.LinkPlatform;
import com.syncboard.domain.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code CreateTicketRequest { title*, description?, category?,
 * priority*, assigneeId?, dueAt?, links?: { url*, linkTitle?, platform? }[] } }.
 */
public record CreateTicketRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        String category,
        @NotNull Priority priority,
        UUID assigneeId,
        Instant dueAt,
        @Valid List<LinkItem> links
) {

    /** Anonymous inline link shape from the contract's {@code links?} array. */
    public record LinkItem(
            @NotBlank @URL String url,
            String linkTitle,
            LinkPlatform platform
    ) {
    }
}
