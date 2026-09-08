// Stage 2/3 — service layer
package com.syncboard.service.impl;

import com.syncboard.api.dto.CreateLinkRequest;
import com.syncboard.api.dto.TicketLinkDto;
import com.syncboard.api.mapper.LinkMapper;
import com.syncboard.persistence.entity.TicketEntity;
import com.syncboard.persistence.entity.TicketLinkEntity;
import com.syncboard.persistence.repository.TicketLinkRepository;
import com.syncboard.persistence.repository.TicketRepository;
import com.syncboard.service.TicketLinkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * {@code TicketLinkEntity} exposes a no-arg constructor and setters {@code setTicket, setUrl,
 * setLinkTitle, setPlatform} — but no {@code setCreatedAt}; that column is stamped by the
 * entity's own {@code @PrePersist}. Listing and deletion go through
 * {@code TicketLinkRepository.findAllByTicketId} / {@code deleteByIdAndTicketId}, real finders,
 * instead of {@code findAll()} plus Java-side filtering.
 */
@Service
@Transactional
public class TicketLinkServiceImpl implements TicketLinkService {

    private final TicketLinkRepository ticketLinkRepository;
    private final TicketRepository ticketRepository;
    private final LinkMapper linkMapper;

    public TicketLinkServiceImpl(TicketLinkRepository ticketLinkRepository, TicketRepository ticketRepository, LinkMapper linkMapper) {
        this.ticketLinkRepository = ticketLinkRepository;
        this.ticketRepository = ticketRepository;
        this.linkMapper = linkMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketLinkDto> listLinks(UUID ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new NoSuchElementException("Ticket not found: " + ticketId);
        }
        return ticketLinkRepository.findAllByTicketId(ticketId).stream()
                .map(linkMapper::toDto)
                .toList();
    }

    @Override
    public TicketLinkDto createLink(UUID ticketId, CreateLinkRequest request) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + ticketId));

        TicketLinkEntity link = new TicketLinkEntity();
        link.setTicket(ticket);
        link.setUrl(request.url());
        link.setLinkTitle(request.linkTitle());
        link.setPlatform(request.platform() != null ? request.platform() : LinkPlatformResolver.resolve(request.url()));
        // createdAt is stamped by TicketLinkEntity's own @PrePersist; no setter exists.
        TicketLinkEntity saved = ticketLinkRepository.save(link);

        return linkMapper.toDto(saved);
    }

    @Override
    public void deleteLink(UUID ticketId, UUID linkId) {
        long deleted = ticketLinkRepository.deleteByIdAndTicketId(linkId, ticketId);
        if (deleted == 0) {
            throw new NoSuchElementException("Link not found: " + linkId);
        }
    }
}
