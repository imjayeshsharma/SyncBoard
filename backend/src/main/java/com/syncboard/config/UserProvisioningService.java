// Stage 5 — Google SSO
package com.syncboard.config;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.syncboard.persistence.repository.UserRepository;

/**
 * Upserts the local {@code users} row for a successfully authenticated Google account.
 *
 * <p><b>Keying:</b> rows are matched on {@code google_subject} (the OIDC {@code sub}
 * claim) — a stable, immutable identifier issued by Google — and never on {@code email},
 * which a Workspace admin can reassign to a different person (see PRD data architecture,
 * "why we never key on email").
 */
@Service
public class UserProvisioningService {

    private final UserRepository userRepository;

    public UserProvisioningService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds the {@code users} row by {@code google_subject} (creating one if this is the
     * account's first login) and refreshes {@code email}, {@code full_name},
     * {@code department} and {@code is_active} from the verified OIDC claims.
     */
    public void upsertFromOidcUser(OidcUser oidcUser) {
        // TODO(stage 5): userRepository.findByGoogleSubject(oidcUser.getSubject())
        //   .orElseGet(() -> a new UserEntity keyed on oidcUser.getSubject())
        //   then set email/fullName/department/isActive from oidcUser claims and save().
        throw new UnsupportedOperationException("TODO stage 5");
    }
}
