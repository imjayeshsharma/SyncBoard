// Stage 2/3 — service layer
package com.syncboard.service;

import com.syncboard.api.dto.CreateLinkRequest;
import com.syncboard.api.dto.TicketLinkDto;

import java.util.List;
import java.util.UUID;

public interface TicketLinkService {

    List<TicketLinkDto> listLinks(UUID ticketId);

    TicketLinkDto createLink(UUID ticketId, CreateLinkRequest request);

    void deleteLink(UUID ticketId, UUID linkId);
}
