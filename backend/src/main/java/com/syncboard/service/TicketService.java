// Stage 2/3 — service layer
package com.syncboard.service;

import com.syncboard.api.dto.CreateTicketRequest;
import com.syncboard.api.dto.PageResponse;
import com.syncboard.api.dto.StatusHistoryEntry;
import com.syncboard.api.dto.TicketDetail;
import com.syncboard.api.dto.TicketSummary;
import com.syncboard.api.dto.TransitionRequest;
import com.syncboard.api.dto.UpdateTicketRequest;
import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    PageResponse<TicketSummary> listTickets(TicketStatus status, UUID assigneeId, Priority priority, String q, int page, int size);

    TicketDetail createTicket(CreateTicketRequest request);

    TicketDetail getTicket(UUID id);

    TicketDetail updateTicket(UUID id, UpdateTicketRequest request);

    /** The one method implemented for real: enforces {@code TransitionRules.check}. */
    TicketDetail transition(UUID id, TransitionRequest request);

    List<StatusHistoryEntry> getHistory(UUID id);
}
