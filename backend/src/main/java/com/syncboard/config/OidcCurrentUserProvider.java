// Stage 5 — Google SSO
package com.syncboard.config;

import java.util.NoSuchElementException;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.syncboard.persistence.entity.UserEntity;
import com.syncboard.persistence.repository.UserRepository;
import com.syncboard.service.CurrentUserProvider;

/**
 * The real, OIDC-backed {@link CurrentUserProvider}. Resolves the authenticated Google
 * account's {@code users} row by the {@code sub} claim on the {@link OidcUser} held in the
 * security context. Active outside the {@code local} profile — under {@code local},
 * {@code service.impl.StubCurrentUserProvider} (owned by A3) stands in so the API is usable
 * before Google credentials exist.
 */
@Service
@Profile("!local")
public class OidcCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public OidcCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity requireCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof OidcUser oidcUser)) {
            throw new IllegalStateException("No authenticated OIDC user in the security context");
        }
        String googleSubject = oidcUser.getSubject();
        return userRepository.findByGoogleSubject(googleSubject)
                .orElseThrow(() -> new NoSuchElementException(
                        "No provisioned user for google_subject=" + googleSubject));
    }
}
