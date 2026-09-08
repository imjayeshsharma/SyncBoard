// Stage 6 — Cache + realtime
package com.syncboard.realtime;

import java.time.Instant;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.syncboard.api.dto.CommentNode;
import com.syncboard.domain.TicketStatus;
import com.syncboard.realtime.event.BoardEvent;
import com.syncboard.realtime.event.BoardEventType;

/**
 * Publishes realtime board and comment events over STOMP to the exact topics fixed by
 * 01-CONTRACT.md §4: {@code /topic/board} and {@code /topic/tickets/{ticketId}/comments}.
 * A3's service layer calls this after a write succeeds — this component has no knowledge
 * of persistence or business rules.
 */
@Component
public class BoardEventPublisher {

    private static final String BOARD_TOPIC = "/topic/board";
    private static final String COMMENTS_TOPIC_TEMPLATE = "/topic/tickets/%s/comments";

    private final SimpMessagingTemplate messagingTemplate;

    public BoardEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** A ticket's status changed, moving it between board columns. */
    public void publishTicketMoved(UUID ticketId, TicketStatus fromStatus, TicketStatus toStatus) {
        publishBoardEvent(BoardEventType.TICKET_MOVED, ticketId, fromStatus, toStatus);
    }

    /** A new ticket was created, landing in {@code initialStatus} (always {@code OPEN} today). */
    public void publishTicketCreated(UUID ticketId, TicketStatus initialStatus) {
        publishBoardEvent(BoardEventType.TICKET_CREATED, ticketId, null, initialStatus);
    }

    /** A ticket's non-status fields changed (title, priority, assignee, etc.). */
    public void publishTicketUpdated(UUID ticketId, TicketStatus currentStatus) {
        publishBoardEvent(BoardEventType.TICKET_UPDATED, ticketId, null, currentStatus);
    }

    /** A new comment was posted (or edited) on {@code ticketId}. */
    public void publishComment(UUID ticketId, CommentNode comment) {
        messagingTemplate.convertAndSend(COMMENTS_TOPIC_TEMPLATE.formatted(ticketId), comment);
    }

    private void publishBoardEvent(BoardEventType type, UUID ticketId, TicketStatus fromStatus, TicketStatus toStatus) {
        messagingTemplate.convertAndSend(BOARD_TOPIC, new BoardEvent(type, ticketId, fromStatus, toStatus, Instant.now()));
    }
}
