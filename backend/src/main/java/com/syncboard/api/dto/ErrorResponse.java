// Stage 2 — REST API DTOs
package com.syncboard.api.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Contract 01-CONTRACT.md §4 — the exact envelope every 4xx/5xx response uses:
 * {@code { timestamp, status, code, message, path, fieldErrors? } }.
 * Produced exclusively by {@link com.syncboard.api.error.GlobalExceptionHandler}.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
