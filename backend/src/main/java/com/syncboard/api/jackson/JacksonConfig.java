// Stage 2 — REST API JSON binding
package com.syncboard.api.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link DomainEnumModule} as a bean so Spring Boot's autoconfigured
 * {@code ObjectMapper} picks it up automatically (any {@code Module} bean is installed).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public DomainEnumModule domainEnumModule() {
        return new DomainEnumModule();
    }
}
