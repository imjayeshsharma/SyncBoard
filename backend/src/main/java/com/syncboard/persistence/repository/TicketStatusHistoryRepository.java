// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.repository;

import com.syncboard.persistence.entity.TicketStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Deliberately exposes no update or delete methods, derived or otherwise:
 * {@code ticket_status_history} is an append-only audit trail (01-CONTRACT.md §3) — a status
 * change, once recorded, must never be edited or removed, even by administrators. Rows are
 * created via {@link TicketStatusHistoryEntity#create}.
 */
public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistoryEntity, UUID> {

    List<TicketStatusHistoryEntity> findAllByTicketIdOrderByChangedAtAsc(UUID ticketId);
}
