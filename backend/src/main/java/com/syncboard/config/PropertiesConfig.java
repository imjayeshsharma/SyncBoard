// Stage 5 — Google SSO
package com.syncboard.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link SyncBoardProperties} as a bean. Kept separate from
 * {@link SecurityConfig} so any A4 class (cache, realtime, GraphQL — not only security) can
 * depend on {@link SyncBoardProperties} without pulling in the whole security configuration.
 */
@Configuration
@EnableConfigurationProperties(SyncBoardProperties.class)
public class PropertiesConfig {
}
