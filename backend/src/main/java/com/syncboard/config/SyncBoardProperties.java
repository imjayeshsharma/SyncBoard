// Stage 5 — Google SSO
package com.syncboard.config;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Binds the {@code syncboard.*} keys declared in {@code application.yml}. Registered via
 * {@link PropertiesConfig}. This is the single place every other A4 class (and, indirectly,
 * anything reading Google Workspace / CORS / cache / realtime settings) reads configuration
 * from — never hardcode a client id, secret, hosted domain or origin anywhere else.
 *
 * @param allowedHostedDomain the Google Workspace hosted-domain claim ("hd") an ID token
 *                            must carry ({@code syncboard.allowed-hosted-domain})
 * @param corsAllowedOrigins  origins allowed to call the API / open a STOMP connection
 *                            ({@code syncboard.cors-allowed-origins})
 * @param board               board-related settings ({@code syncboard.board.*})
 * @param realtime            STOMP/WebSocket settings ({@code syncboard.realtime.*})
 */
@Validated
@ConfigurationProperties("syncboard")
public record SyncBoardProperties(
        @NotBlank String allowedHostedDomain,
        @NotEmpty List<String> corsAllowedOrigins,
        @NotNull Board board,
        @NotNull Realtime realtime) {

    /** {@code syncboard.board.*} — read-model caching. */
    public record Board(@Positive int cacheTtlSeconds) {
    }

    /** {@code syncboard.realtime.*} — STOMP endpoint and destination prefixes. */
    public record Realtime(
            @NotBlank String stompEndpoint,
            @NotBlank String brokerPrefix,
            @NotBlank String appPrefix) {
    }
}
