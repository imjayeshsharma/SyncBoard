// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.repository;

import com.syncboard.domain.TicketStatus;
import com.syncboard.persistence.entity.TicketEntity;
import com.syncboard.persistence.projection.TicketBoardRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findAllByStatusIn(Collection<TicketStatus> statuses);

    List<TicketEntity> findAllByAssigneeId(UUID assigneeId);

    /**
     * Fetch-joins reporter and assignee so callers that need full {@code TicketEntity} graphs
     * (e.g. ticket list/detail assembly in A3) avoid one N+1 query per ticket. Prefer
     * {@link #findBoardRows()} for the board endpoint itself, which loads no entity graph at all.
     */
    @Query("""
            select t from TicketEntity t
            join fetch t.reporter
            left join fetch t.assignee
            """)
    List<TicketEntity> findAllWithReporterAndAssignee();

    /**
     * The board-load query: a single round trip that joins reporter/assignee and computes
     * per-ticket comment/link counts via correlated subqueries, projected straight onto
     * {@link TicketBoardRow} so no {@code TicketEntity}/{@code UserEntity} graph is ever
     * materialized for the board view.
     */
    @Query("""
            select
                t.id as id,
                t.title as title,
                t.status as status,
                t.priority as priority,
                t.category as category,
                r.id as reporterId,
                r.fullName as reporterFullName,
                r.email as reporterEmail,
                r.department as reporterDepartment,
                r.active as reporterActive,
                a.id as assigneeId,
                a.fullName as assigneeFullName,
                a.email as assigneeEmail,
                a.department as assigneeDepartment,
                a.active as assigneeActive,
                t.dueAt as dueAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.version as version,
                (select count(c) from CommentEntity c where c.ticket = t) as commentCount,
                (select count(l) from TicketLinkEntity l where l.ticket = t) as linkCount
            from TicketEntity t
            join t.reporter r
            left join t.assignee a
            """)
    List<TicketBoardRow> findBoardRows();
}
