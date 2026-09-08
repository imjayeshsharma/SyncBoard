// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.repository;

import com.syncboard.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByGoogleSubject(String googleSubject);

    List<UserEntity> findAllByActiveTrue();
}
