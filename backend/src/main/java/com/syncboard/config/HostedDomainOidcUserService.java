// Stage 5 — Google SSO
package com.syncboard.config;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Enforces the PRD's Google Workspace restriction on top of Spring Security's standard
 * OIDC login flow.
 *
 * <p><b>Trust boundary:</b> by the time {@link #loadUser(OidcUserRequest)} runs, the ID
 * token's signature, issuer, audience and expiry have already been verified by Spring
 * Security's {@code JwtDecoder} against Google's published JWKS (configured from the
 * {@code spring.security.oauth2.client.provider.google.issuer-uri} in {@code
 * application.yml}) — none of that is trusted from, or re-derived from, anything the
 * browser sends. This class only adds the SyncBoard-specific check that the verified
 * {@code hd} (hosted domain) claim matches {@link SyncBoardProperties#allowedHostedDomain()},
 * and triggers user provisioning on success.
 */
@Service
public class HostedDomainOidcUserService extends OidcUserService {

    private final SyncBoardProperties properties;
    private final UserProvisioningService userProvisioningService;

    public HostedDomainOidcUserService(SyncBoardProperties properties,
            UserProvisioningService userProvisioningService) {
        this.properties = properties;
        this.userProvisioningService = userProvisioningService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String hostedDomain = oidcUser.getIdToken().getClaimAsString("hd");
        if (hostedDomain == null || !hostedDomain.equalsIgnoreCase(properties.allowedHostedDomain())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "FORBIDDEN_DOMAIN",
                    "Google account is not part of the " + properties.allowedHostedDomain() + " workspace",
                    null));
        }

        userProvisioningService.upsertFromOidcUser(oidcUser);
        return oidcUser;
    }
}
