// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Clock;

/**
 * Wires up the persistence package: repository scanning, JPA auditing infrastructure (for
 * future {@code @CreatedDate}/{@code @LastModifiedDate} use — entities in this stage stamp
 * {@code createdAt}/{@code updatedAt} explicitly via {@code @PrePersist}/{@code @PreUpdate}),
 * and a single UTC {@link Clock} bean so every layer computes "now" the same way.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.syncboard.persistence.repository")
public class PersistenceConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
