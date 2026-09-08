// Stage 5 — Google SSO
package com.syncboard.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stage 5 web security wiring: Google Workspace SSO via OIDC, a stateless-ish JSON API
 * under {@code /api/v1/**}, GraphQL under {@code /graphql} and STOMP under {@code /ws/**}
 * all requiring authentication, and CORS driven entirely by
 * {@link SyncBoardProperties#corsAllowedOrigins()}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final HostedDomainOidcUserService hostedDomainOidcUserService;

    public SecurityConfig(HostedDomainOidcUserService hostedDomainOidcUserService) {
        this.hostedDomainOidcUserService = hostedDomainOidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF is disabled: every mutating endpoint under /api/v1/** is a JSON API
                // consumed only by the Angular SPA (and, over STOMP, /ws/**) — there is no
                // browser-submitted HTML form for CSRF to protect, and the OAuth2
                // authorization-code redirect itself is a top-level GET navigation that
                // Spring Security's CSRF filter never intercepts. Revisit if a
                // server-rendered form is ever added.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/v1/**", "/graphql", "/ws/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(hostedDomainOidcUserService)));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SyncBoardProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.corsAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
