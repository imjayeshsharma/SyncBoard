// Stage 2/3 — service layer (current-user seam; A4 supplies the real OIDC-backed impl at stage 5)
package com.syncboard.service;

import com.syncboard.persistence.entity.UserEntity;

/**
 * Seam for "who is making this request". {@link com.syncboard.service.impl.StubCurrentUserProvider}
 * satisfies this locally (profile {@code local}) before Stage 5 wires up the real Google
 * OIDC-backed implementation (owned by A4, in {@code com.syncboard.config} / security).
 */
public interface CurrentUserProvider {

    UserEntity requireCurrentUser();
}
