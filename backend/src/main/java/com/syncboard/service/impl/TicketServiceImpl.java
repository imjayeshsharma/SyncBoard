// Stage 2/3 — service layer
package com.syncboard.service.impl;

import com.syncboard.api.dto.CreateTicketRequest;
import com.syncboard.api.dto.PageResponse;
import com.syncboard.api.dto.StatusHistoryEntry;
import com.syncboard.api.dto.TicketDetail;
import com.syncboard.api.dto.TicketSummary;
import com.syncboard.api.dto.TransitionRequest;
import com.syncboard.api.dto.UpdateTicketRequest;
import com.syncboard.api.mapper.TicketMapper;
import com.syncboard.domain.IllegalTransitionException;
import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;
import com.syncboard.domain.TransitionCheck;
import com.syncboard.domain.TransitionContext;
import com.syncboard.domain.TransitionRules;
import com.syncboard.persistence.entity.CommentEntity;
import com.syncboard.persistence.entity.TicketEntity;
import com.syncboard.persistence.entity.TicketLinkEntity;
import com.syncboard.persistence.entity.TicketStatusHistoryEntity;
import com.syncboard.persistence.entity.UserEntity;
import com.syncboard.persistence.repository.CommentRepository;
import com.syncboard.persistence.repository.TicketLinkRepository;
import com.syncboard.persistence.repository.TicketRepository;
import com.syncboard.persistence.repository.TicketStatusHistoryRepository;
import com.syncboard.persistence.repository.UserRepository;
import com.syncboard.service.CurrentUserProvider;
import com.syncboard.service.TicketService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Ticket read/write orchestration. Depends on the actual A1/A2 shapes (verified against their
 * landed source, not guessed):
 * <ul>
 *   <li>{@code TransitionContext(current, target, blockedFrom, hasAssignee, note)} —
 *       {@code hasAssignee} is a {@code boolean}, not an assignee id.</li>
 *   <li>{@code TransitionRules.check(TransitionContext)} returns a sealed
 *       {@code TransitionCheck}: {@code Allowed(resultingBlockedFromStatus)} or
 *       {@code Rejected(DomainErrorCode code, String message)}.</li>
 *   <li>{@code IllegalTransitionException.from(TransitionCheck.Rejected)} builds the exception
 *       that {@link com.syncboard.api.error.GlobalExceptionHandler} maps via {@code ex.code()}
 *       (declared on the abstract {@code DomainException} supertype).</li>
 *   <li>{@code TicketEntity} exposes {@code getVersion()} but no {@code setVersion}/
 *       {@code setUpdatedAt}/{@code setCreatedAt} — those columns are owned by {@code @Version}/
 *       {@code @PreUpdate}/{@code @PrePersist}. Optimistic locking is enforced here by comparing
 *       {@code ticket.getVersion()} against the request's version up front and throwing
 *       {@link ObjectOptimisticLockingFailureException} (mapped to 409 VERSION_CONFLICT) rather
 *       than by writing the version back onto the entity.</li>
 *   <li>{@code TicketLinkEntity} has no {@code setCreatedAt} either — {@code @PrePersist} stamps
 *       it.</li>
 *   <li>{@code TicketStatusHistoryEntity} has no public constructor or setters; rows are created
 *       exclusively via the static {@code TicketStatusHistoryEntity.create(...)} factory.</li>
 *   <li>{@code TicketLinkRepository.findAllByTicketId}, {@code CommentRepository
 *       .findAllByTicketIdOrderByCreatedAtAsc}, and {@code TicketStatusHistoryRepository
 *       .findAllByTicketIdOrderByChangedAtAsc} are real finders — no need to {@code findAll()}
 *       and filter in Java.</li>
 * </ul>
 */
@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final CommentRepository commentRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final TicketMapper ticketMapper;
    private final CurrentUserProvider currentUserProvider;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketLinkRepository ticketLinkRepository,
            CommentRepository commentRepository,
            TicketStatusHistoryRepository ticketStatusHistoryRepository,
            TicketMapper ticketMapper,
            CurrentUserProvider currentUserProvider
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.commentRepository = commentRepository;
        this.ticketStatusHistoryRepository = ticketStatusHistoryRepository;
        this.ticketMapper = ticketMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketSummary> listTickets(TicketStatus status, UUID assigneeId, Priority priority, String q, int page, int size) {
        // TODO(stage 2): filtered + paginated ticket search (status/assigneeId/priority/q).
        // Needs either JPA Specifications or hand-written query methods on TicketRepository
        // that don't exist in the shared contract yet; left as a stub rather than guessed.
        throw new UnsupportedOperationException("TODO stage 2: filtered/paginated ticket search");
    }

    @Override
    public TicketDetail createTicket(CreateTicketRequest request) {
        UserEntity reporter = currentUserProvider.requireCurrentUser();
        UserEntity assignee = request.assigneeId() != null
                ? userRepository.findById(request.assigneeId())
                        .orElseThrow(() -> new NoSuchElementException("User not found: " + request.assigneeId()))
                : null;

        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(request.priority());
        ticket.setReporter(reporter);
        ticket.setAssignee(assignee);
        ticket.setDueAt(request.dueAt());
        // createdAt/updatedAt are stamped by TicketEntity's own @PrePersist.
        TicketEntity saved = ticketRepository.save(ticket);

        if (request.links() != null) {
            for (CreateTicketRequest.LinkItem item : request.links()) {
                TicketLinkEntity link = new TicketLinkEntity();
                link.setTicket(saved);
                link.setUrl(item.url());
                link.setLinkTitle(item.linkTitle());
                link.setPlatform(item.platform() != null ? item.platform() : LinkPlatformResolver.resolve(item.url()));
                // createdAt is stamped by TicketLinkEntity's own @PrePersist; no setter exists.
                ticketLinkRepository.save(link);
            }
        }

        return getTicket(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetail getTicket(UUID id) {
        TicketEntity ticket = findTicketOrThrow(id);
        return ticketMapper.toDetail(ticket, findLinks(id), findComments(id), findHistory(id));
    }

    @Override
    public TicketDetail updateTicket(UUID id, UpdateTicketRequest request) {
        TicketEntity ticket = findTicketOrThrow(id);
        requireMatchingVersion(ticket, request.version());

        if (request.title() != null) {
            ticket.setTitle(request.title());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.category() != null) {
            ticket.setCategory(request.category());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.assigneeId() != null) {
            UserEntity assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + request.assigneeId()));
            ticket.setAssignee(assignee);
        }
        if (request.dueAt() != null) {
            ticket.setDueAt(request.dueAt());
        }
        // updatedAt is stamped by TicketEntity's own @PreUpdate; version is bumped by @Version.

        TicketEntity saved = ticketRepository.save(ticket);
        return getTicket(saved.getId());
    }

    /**
     * Load the ticket, build a {@code TransitionContext}, ask {@code TransitionRules.check},
     * throw on rejection, otherwise apply the new status — maintaining {@code blockedFromStatus}
     * (taken straight from {@code TransitionCheck.Allowed.resultingBlockedFromStatus()}) and
     * {@code closedAt} — append an append-only history row, and let {@code @Version} surface
     * concurrent-write conflicts.
     */
    @Override
    public TicketDetail transition(UUID id, TransitionRequest request) {
        TicketEntity ticket = findTicketOrThrow(id);
        requireMatchingVersion(ticket, request.version());

        TicketStatus fromStatus = ticket.getStatus();
        TicketStatus toStatus = request.toStatus();

        TransitionContext context = new TransitionContext(
                fromStatus,
                toStatus,
                ticket.getBlockedFromStatus(),
                ticket.getAssignee() != null,
                request.note()
        );

        TransitionCheck check = TransitionRules.check(context);
        TicketStatus resultingBlockedFromStatus = switch (check) {
            case TransitionCheck.Allowed allowed -> allowed.resultingBlockedFromStatus();
            case TransitionCheck.Rejected rejected -> throw IllegalTransitionException.from(rejected);
        };

        UserEntity actor = currentUserProvider.requireCurrentUser();

        ticket.setBlockedFromStatus(resultingBlockedFromStatus);
        ticket.setStatus(toStatus);
        if (toStatus == TicketStatus.COMPLETED || toStatus == TicketStatus.CANCELLED) {
            ticket.setClosedAt(Instant.now());
        }
        // updatedAt is stamped by TicketEntity's own @PreUpdate; version is bumped by @Version.
        TicketEntity saved = ticketRepository.save(ticket);

        TicketStatusHistoryEntity historyEntry = TicketStatusHistoryEntity.create(
                saved, fromStatus, toStatus, actor, Instant.now(), request.note());
        ticketStatusHistoryRepository.save(historyEntry);

        return getTicket(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusHistoryEntry> getHistory(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new NoSuchElementException("Ticket not found: " + id);
        }
        return findHistory(id).stream().map(ticketMapper::toHistoryEntry).toList();
    }

    /**
     * Optimistic-lock precondition: pins the update to the version the client last read.
     * Hibernate's own {@code @Version} check would eventually surface a stale write too, but only
     * after issuing the UPDATE; failing fast here avoids that round trip and produces the same
     * {@link ObjectOptimisticLockingFailureException} -> 409 VERSION_CONFLICT mapping.
     */
    private void requireMatchingVersion(TicketEntity ticket, long expectedVersion) {
        if (ticket.getVersion() != expectedVersion) {
            throw new ObjectOptimisticLockingFailureException(TicketEntity.class, ticket.getId());
        }
    }

    private TicketEntity findTicketOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + id));
    }

    private List<TicketLinkEntity> findLinks(UUID ticketId) {
        return ticketLinkRepository.findAllByTicketId(ticketId);
    }

    private List<CommentEntity> findComments(UUID ticketId) {
        return commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    private List<TicketStatusHistoryEntity> findHistory(UUID ticketId) {
        return ticketStatusHistoryRepository.findAllByTicketIdOrderByChangedAtAsc(ticketId);
    }
}
