// Stage 2/3 — service layer
package com.syncboard.service;

import com.syncboard.api.dto.UserSummary;

import java.util.List;

public interface UserService {

    List<UserSummary> listUsers(boolean activeOnly);

    UserSummary getCurrentUser();
}
