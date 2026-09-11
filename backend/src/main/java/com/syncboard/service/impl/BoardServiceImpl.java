// Stage 2/3 — service layer
package com.syncboard.service.impl;

import com.syncboard.api.dto.BoardColumn;
import com.syncboard.api.dto.BoardResponse;
import com.syncboard.api.dto.TicketSummary;
import com.syncboard.api.dto.UserSummary;
import com.syncboard.domain.Overdue;
import com.syncboard.domain.TicketStatus;
import com.syncboard.persistence.projection.TicketBoardRow;
import com.syncboard.persistence.repository.TicketRepository;
import com.syncboard.service.BoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the board via {@code TicketRepository.findBoardRows()} — the real, purpose-built finder
 * that projects straight onto {@link TicketBoardRow} (reporter/assignee already flattened in,
 * per-ticket comment/link counts computed in SQL) so no {@code TicketEntity}/{@code UserEntity}
 * graph and no separate comment/link count queries are needed for the board view.
 * {@code overdue} is still always computed via {@link Overdue}, never read off a row.
 */
@Service
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    public BoardServiceImpl(TicketRepository ticketRepository, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.clock = clock;
    }

    @Override
    public BoardResponse getBoard() {
        Instant now = Instant.now(clock);
        List<TicketBoardRow> rows = ticketRepository.findBoardRows();

        Map<TicketStatus, List<TicketSummary>> byStatus = new EnumMap<>(TicketStatus.class);
        for (TicketBoardRow row : rows) {
            byStatus.computeIfAbsent(row.getStatus(), key -> new ArrayList<>()).add(toSummary(row, now));
        }

        List<BoardColumn> columns = new ArrayList<>();
        for (TicketStatus status : TicketStatus.boardColumns()) {
            columns.add(new BoardColumn(status, boardLabel(status), byStatus.getOrDefault(status, List.of())));
        }
        return new BoardResponse(columns, now);
    }

    private TicketSummary toSummary(TicketBoardRow row, Instant now) {
        boolean overdue = Overdue.isOverdue(row.getDueAt(), row.getStatus(), now);
        UserSummary reporter = new UserSummary(
                row.getReporterId(), row.getReporterFullName(), row.getReporterEmail(),
                row.getReporterDepartment(), row.isReporterActive());
        UserSummary assignee = row.getAssigneeId() == null
                ? null
                : new UserSummary(
                        row.getAssigneeId(), row.getAssigneeFullName(), row.getAssigneeEmail(),
                        row.getAssigneeDepartment(), Boolean.TRUE.equals(row.isAssigneeActive()));

        return new TicketSummary(
                row.getId(),
                row.getTitle(),
                row.getStatus(),
                row.getPriority(),
                row.getCategory(),
                assignee,
                reporter,
                row.getDueAt(),
                overdue,
                (int) row.getCommentCount(),
                (int) row.getLinkCount(),
                row.getCreatedAt(),
                row.getUpdatedAt(),
                row.getVersion()
        );
    }

    private String boardLabel(TicketStatus status) {
        return switch (status) {
            case OPEN -> "Open";
            case TRIAGING -> "Triaging";
            case IN_PROGRESS -> "In Progress";
            case UNDER_REVIEW -> "Under Review";
            case BLOCKED -> "Blocked";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
        };
    }
}
