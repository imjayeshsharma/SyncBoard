// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import com.syncboard.domain.LinkPlatform;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Contract 01-CONTRACT.md §4: {@code CreateLinkRequest { url*, linkTitle?, platform? } }.
 * When {@code platform} is omitted, it is derived server-side from the URL host
 * (contract §4: {@code keka | google-drive | google-docs | github | slack | other}).
 */
public record CreateLinkRequest(
        @NotBlank @URL String url,
        String linkTitle,
        LinkPlatform platform
) {
}
