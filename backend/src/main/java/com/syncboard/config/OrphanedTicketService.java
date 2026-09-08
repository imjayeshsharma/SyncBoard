// Stage 5 — Google SSO
package com.syncboard.config;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.syncboard.persistence.repository.TicketRepository;
import com.syncboard.persistence.repository.TicketStatusHistoryRepository;

/**
 * PRD "Orphaned Tickets": when a user is deactivated (e.g. offboarded from Workspace),
 * their assigned, still-active tickets must not silently disappear from anyone's view —
 * they are unassigned and the change is recorded.
 */
@Service
public class OrphanedTicketService {

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;

    public OrphanedTicketService(TicketRepository ticketRepository,
            TicketStatusHistoryRepository ticketStatusHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketStatusHistoryRepository = ticketStatusHistoryRepository;
    }

    /**
     * Unsets {@code assignee_id} on every active (non-terminal-status) ticket currently
     * assigned to {@code userId}, appending one {@code ticket_status_history} row per
     * affected ticket (status unchanged, {@code note} explaining the reassignment) so the
     * change is auditable.
     */
    public void reassignTicketsOfDeactivatedUser(UUID userId) {
        // TODO(stage 5): ticketRepository.findAllByAssigneeId(userId), filter to active
        // statuses, clear assigneeId, save; for each, append a history row via
        // ticketStatusHistoryRepository recording fromStatus == toStatus and a note such as
        // "assignee deactivated".
        throw new UnsupportedOperationException("TODO stage 5");
    }
}
