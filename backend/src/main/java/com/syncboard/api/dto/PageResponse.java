// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contract 01-CONTRACT.md §4: {@code PageResponse<T> { content: T[], page, size, totalElements, totalPages } }.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /** Convenience factory from a Spring Data {@link Page} of already-mapped DTOs. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
