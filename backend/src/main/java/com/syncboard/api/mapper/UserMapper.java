// Stage 2 — REST API mappers
package com.syncboard.api.mapper;

import com.syncboard.api.dto.UserSummary;
import com.syncboard.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Entity to DTO mapping for {@link UserEntity} -&gt; {@link UserSummary}.
 *
 * <p>Assumes {@code UserEntity} exposes {@code getId, getFullName, getEmail, getDepartment,
 * isActive} — A2 owns this entity and had not landed it at the time this file was written.
 */
@Component
public class UserMapper {

    public UserSummary toSummary(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserSummary(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getDepartment(),
                entity.isActive()
        );
    }
}
