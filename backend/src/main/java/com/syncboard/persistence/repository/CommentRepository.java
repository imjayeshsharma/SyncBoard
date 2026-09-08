// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.repository;

import com.syncboard.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    List<CommentEntity> findAllByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
