// Stage 2 — REST API mappers
package com.syncboard.api.mapper;

import com.syncboard.api.dto.CommentNode;
import com.syncboard.api.dto.StatusHistoryEntry;
import com.syncboard.api.dto.TicketDetail;
import com.syncboard.api.dto.TicketLinkDto;
import com.syncboard.api.dto.TicketSummary;
import com.syncboard.domain.Overdue;
import com.syncboard.persistence.entity.CommentEntity;
import com.syncboard.persistence.entity.TicketEntity;
import com.syncboard.persistence.entity.TicketLinkEntity;
import com.syncboard.persistence.entity.TicketStatusHistoryEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Entity to DTO mapping for {@link TicketEntity} -&gt; {@link TicketSummary} / {@link TicketDetail}.
 * {@code overdue} is always computed via {@link Overdue}, never read off the entity
 * (contract 01-CONTRACT.md §3: "overdue is never stored").
 *
 * <p>{@code TicketEntity} exposes plain getters for every {@code tickets} column from contract §3
 * (id, title, description, status, priority, category, reporter, assignee, blockedFromStatus,
 * dueAt, createdAt, updatedAt, closedAt, version — version is a primitive {@code long}), with
 * {@code reporter} and {@code assignee} mapped as {@code UserEntity} relations.
 * {@code com.syncboard.domain.Overdue.isOverdue(Instant dueAt, TicketStatus status, Instant now)}
 * takes "now" as an explicit third argument rather than defaulting it internally, so this mapper
 * supplies it from the shared {@link Clock} bean ({@code com.syncboard.persistence
 * .PersistenceConfig}) that every layer uses to compute "now" the same way.
 */
@Component
public class TicketMapper {

    private final UserMapper userMapper;
    private final LinkMapper linkMapper;
    private final CommentMapper commentMapper;
    private final Clock clock;

    public TicketMapper(UserMapper userMapper, LinkMapper linkMapper, CommentMapper commentMapper, Clock clock) {
        this.userMapper = userMapper;
        this.linkMapper = linkMapper;
        this.commentMapper = commentMapper;
        this.clock = clock;
    }

    public TicketSummary toSummary(TicketEntity entity, int commentCount, int linkCount) {
        boolean overdue = Overdue.isOverdue(entity.getDueAt(), entity.getStatus(), Instant.now(clock));
        return new TicketSummary(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getCategory(),
                userMapper.toSummary(entity.getAssignee()),
                userMapper.toSummary(entity.getReporter()),
                entity.getDueAt(),
                overdue,
                commentCount,
                linkCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public TicketDetail toDetail(
            TicketEntity entity,
            List<TicketLinkEntity> linkEntities,
            List<CommentEntity> commentEntities,
            List<TicketStatusHistoryEntity> historyEntities
    ) {
        boolean overdue = Overdue.isOverdue(entity.getDueAt(), entity.getStatus(), Instant.now(clock));
        List<TicketLinkDto> links = linkEntities.stream().map(linkMapper::toDto).toList();
        List<CommentNode> comments = commentMapper.toTree(commentEntities);
        List<StatusHistoryEntry> history = historyEntities.stream().map(this::toHistoryEntry).toList();

        return new TicketDetail(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getCategory(),
                userMapper.toSummary(entity.getAssignee()),
                userMapper.toSummary(entity.getReporter()),
                entity.getDueAt(),
                overdue,
                commentEntities.size(),
                linkEntities.size(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                entity.getDescription(),
                entity.getBlockedFromStatus(),
                entity.getClosedAt(),
                links,
                comments,
                history
        );
    }

    public StatusHistoryEntry toHistoryEntry(TicketStatusHistoryEntity entity) {
        return new StatusHistoryEntry(
                entity.getId(),
                entity.getFromStatus(),
                entity.getToStatus(),
                userMapper.toSummary(entity.getChangedBy()),
                entity.getChangedAt(),
                entity.getNote()
        );
    }
}
