// Stage 2/3 — service layer
package com.syncboard.service.impl;

import com.syncboard.api.dto.CommentNode;
import com.syncboard.api.dto.CreateCommentRequest;
import com.syncboard.api.mapper.CommentMapper;
import com.syncboard.api.mapper.UserMapper;
import com.syncboard.persistence.entity.CommentEntity;
import com.syncboard.persistence.entity.TicketEntity;
import com.syncboard.persistence.entity.UserEntity;
import com.syncboard.persistence.repository.CommentRepository;
import com.syncboard.persistence.repository.TicketRepository;
import com.syncboard.service.CommentService;
import com.syncboard.service.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * {@code CommentEntity} threads replies via a real {@code parentComment} relation
 * ({@code getParentComment()}/{@code setParentComment(CommentEntity)}), not a raw parent-id
 * column — so creating a reply means loading the parent {@code CommentEntity} first. A2 also
 * supplies the {@code CommentEntity(TicketEntity, UserEntity, String, CommentEntity)}
 * all-args constructor used below; {@code createdAt} is stamped by the entity's own
 * {@code @PrePersist} (no {@code setCreatedAt} exists). Listing goes through
 * {@code CommentRepository.findAllByTicketIdOrderByCreatedAtAsc}, a real finder, instead of
 * {@code findAll()} plus Java-side filtering.
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            CommentMapper commentMapper,
            UserMapper userMapper,
            CurrentUserProvider currentUserProvider
    ) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentNode> listComments(UUID ticketId) {
        requireTicketExists(ticketId);
        List<CommentEntity> flat = commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId);
        return commentMapper.toTree(flat);
    }

    @Override
    public CommentNode createComment(UUID ticketId, CreateCommentRequest request) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + ticketId));
        UserEntity author = currentUserProvider.requireCurrentUser();

        CommentEntity parentComment = request.parentCommentId() != null
                ? commentRepository.findById(request.parentCommentId())
                        .orElseThrow(() -> new NoSuchElementException("Comment not found: " + request.parentCommentId()))
                : null;

        CommentEntity comment = new CommentEntity(ticket, author, request.bodyText(), parentComment);
        CommentEntity saved = commentRepository.save(comment);

        return new CommentNode(saved.getId(), saved.getBodyText(), userMapper.toSummary(author), saved.getCreatedAt(), saved.getEditedAt(), List.of());
    }

    private void requireTicketExists(UUID ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new NoSuchElementException("Ticket not found: " + ticketId);
        }
    }
}
