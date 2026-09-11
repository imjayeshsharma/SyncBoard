// Stage 7 — GraphQL read API
package com.syncboard.graphql;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.syncboard.api.dto.BoardResponse;
import com.syncboard.api.dto.CommentNode;
import com.syncboard.api.dto.StatusHistoryEntry;
import com.syncboard.api.dto.TicketDetail;
import com.syncboard.api.dto.TicketLinkDto;
import com.syncboard.service.BoardService;
import com.syncboard.service.CurrentUserProvider;
import com.syncboard.service.TicketService;

/**
 * Read-only GraphQL surface (01-CONTRACT.md §4, stage 7). Delegates to A3's service layer —
 * no persistence access or DTO mapping logic lives here.
 *
 * <p>Note on the {@code Ticket} GraphQL type: the {@code board} query's nested tickets are
 * backed by {@code TicketSummary} (per {@code BoardResponse.columns[].tickets}), which has
 * no {@code links}/{@code comments}/{@code history} — those fields only exist on
 * {@code TicketDetail}, returned by {@code ticket}/{@code myTickets}. The
 * {@code @SchemaMapping} methods below resolve those three fields for both source shapes:
 * for a {@code TicketDetail} they are already populated, for a {@code TicketSummary} they
 * must be batch-loaded by ticket id (the classic board N+1) — a {@code DataLoader} is the
 * intended stage-7 implementation.
 */
@Controller
public class BoardGraphQlController {

    private final BoardService boardService;
    private final TicketService ticketService;
    private final CurrentUserProvider currentUserProvider;

    public BoardGraphQlController(BoardService boardService, TicketService ticketService,
            CurrentUserProvider currentUserProvider) {
        this.boardService = boardService;
        this.ticketService = ticketService;
        this.currentUserProvider = currentUserProvider;
    }

    @QueryMapping
    public BoardResponse board() {
        // TODO(stage 7): delegate to boardService's board-read method.
        throw new UnsupportedOperationException("TODO stage 7");
    }

    @QueryMapping
    public TicketDetail ticket(@Argument UUID id) {
        // TODO(stage 7): delegate to ticketService's find-by-id method; return null (not
        // throw) when absent, since the schema declares `ticket(id: ID!): Ticket` nullable.
        throw new UnsupportedOperationException("TODO stage 7");
    }

    @QueryMapping
    public List<TicketDetail> myTickets() {
        // TODO(stage 7): currentUserProvider.requireCurrentUser() then
        // ticketService's find-by-assignee method.
        throw new UnsupportedOperationException("TODO stage 7");
    }

    @SchemaMapping(typeName = "Ticket", field = "comments")
    public List<CommentNode> comments(Object ticket) {
        // TODO(stage 7): if `ticket` is a TicketDetail, return its comments directly; if a
        // TicketSummary (nested board tickets), batch-load via a DataLoader keyed on
        // ticket id to avoid one comments query per ticket.
        throw new UnsupportedOperationException("TODO stage 7");
    }

    @SchemaMapping(typeName = "Ticket", field = "links")
    public List<TicketLinkDto> links(Object ticket) {
        // TODO(stage 7): same batching strategy as comments(Object).
        throw new UnsupportedOperationException("TODO stage 7");
    }

    @SchemaMapping(typeName = "Ticket", field = "history")
    public List<StatusHistoryEntry> history(Object ticket) {
        // TODO(stage 7): same batching strategy as comments(Object).
        throw new UnsupportedOperationException("TODO stage 7");
    }
}
