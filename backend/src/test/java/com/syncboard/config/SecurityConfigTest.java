// Stage 5 — Google SSO
package com.syncboard.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test for {@link SecurityConfig}.
 *
 * <p>Only the {@code /api/v1/board} → 401 case is reliably exercisable inside a
 * {@code @WebMvcTest} slice: Spring Security's filter chain rejects the unauthenticated
 * request before any {@code @Controller} handler mapping is consulted, so
 * {@code api.controller.BoardController} does not need to exist yet for this test to be
 * meaningful. {@link HostedDomainOidcUserService} is mocked so the OAuth2 login DSL has
 * something to wire up without needing its own dependency chain (properties, persistence)
 * inside the slice.
 *
 * <p>Actuator's {@code /actuator/health} endpoint is not part of this MVC slice at all
 * ({@code @WebMvcTest} does not auto-configure Actuator's web endpoints), so verifying it
 * is public needs a full {@code @SpringBootTest} — left disabled until stage 5 lands with
 * real Google credentials to test the whole login flow end to end.
 */
@WebMvcTest
@Import(SecurityConfig.class)
@ImportAutoConfiguration(OAuth2ClientAutoConfiguration.class)
@EnableConfigurationProperties(SyncBoardProperties.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HostedDomainOidcUserService hostedDomainOidcUserService;

    @Test
    void apiEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/board"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("TODO stage 5 — needs a full @SpringBootTest context for Actuator's web endpoints")
    void actuatorHealthIsPublic() {
    }

    @Test
    @Disabled("TODO stage 5 — Google OAuth2 login redirect, pending real client credentials")
    void oauth2LoginRedirectsToGoogle() {
    }

    @Test
    @Disabled("TODO stage 5 — hosted-domain rejection, pending HostedDomainOidcUserService integration test")
    void nonWorkspaceAccountIsRejected() {
    }
}
