// Stage 2/3 — current-user seam (local dev stub; A4 supplies the real OIDC-backed impl at stage 5)
package com.syncboard.service.impl;

import com.syncboard.persistence.entity.UserEntity;
import com.syncboard.persistence.repository.UserRepository;
import com.syncboard.service.CurrentUserProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Returns the first seeded dev user so the API is usable before Stage 5's real OIDC-backed
 * {@code CurrentUserProvider} (A4) exists. Active only under the {@code local} profile.
 *
 * <p>Relies only on {@code UserRepository.findAll()}, guaranteed by {@code JpaRepository}, to
 * avoid depending on a custom finder method name A2 may not have added yet.
 */
@Component
@Profile("local")
public class StubCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public StubCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity requireCurrentUser() {
        return userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No seeded users available for local development"));
    }
}
