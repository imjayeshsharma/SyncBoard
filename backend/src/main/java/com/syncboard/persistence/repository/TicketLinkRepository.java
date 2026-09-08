// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.repository;

import com.syncboard.persistence.entity.TicketLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketLinkRepository extends JpaRepository<TicketLinkEntity, UUID> {

    List<TicketLinkEntity> findAllByTicketId(UUID ticketId);

    /** @return number of rows deleted (0 or 1) — lets the caller distinguish a not-found link. */
    long deleteByIdAndTicketId(UUID id, UUID ticketId);
}
