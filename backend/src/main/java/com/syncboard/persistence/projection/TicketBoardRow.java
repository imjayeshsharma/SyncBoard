// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.projection;

import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Closed interface projection supplying exactly the columns {@code TicketSummary}
 * (01-CONTRACT.md §4) needs for the board view, so
 * {@link com.syncboard.persistence.repository.TicketRepository#findBoardRows()} never loads full
 * {@code TicketEntity}/{@code UserEntity} graphs. {@code overdue} and {@code commentCount ==}
 * {@code linkCount == 0} handling stay derived fields computed by the service layer (A3), not
 * stored here.
 *
 * <p>Assignee fields are {@code null} when the ticket is unassigned.
 */
public interface TicketBoardRow {

    UUID getId();

    String getTitle();

    TicketStatus getStatus();

    Priority getPriority();

    String getCategory();

    UUID getReporterId();

    String getReporterFullName();

    String getReporterEmail();

    String getReporterDepartment();

    boolean isReporterActive();

    UUID getAssigneeId();

    String getAssigneeFullName();

    String getAssigneeEmail();

    String getAssigneeDepartment();

    Boolean isAssigneeActive();

    Instant getDueAt();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    long getVersion();

    long getCommentCount();

    long getLinkCount();
}
