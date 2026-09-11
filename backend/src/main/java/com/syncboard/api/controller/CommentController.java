// Stage 2 — REST API controllers
package com.syncboard.api.controller;

import com.syncboard.api.dto.CommentNode;
import com.syncboard.api.dto.CreateCommentRequest;
import com.syncboard.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Contract 01-CONTRACT.md §4: {@code /api/v1/tickets/{id}/comments}. No logic here —
 * delegates to {@link CommentService}.
 */
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentNode> listComments(@PathVariable UUID ticketId) {
        return commentService.listComments(ticketId);
    }

    @PostMapping
    public ResponseEntity<CommentNode> createComment(@PathVariable UUID ticketId, @Valid @RequestBody CreateCommentRequest request) {
        CommentNode created = commentService.createComment(ticketId, request);
        return ResponseEntity.created(URI.create("/api/v1/tickets/" + ticketId + "/comments/" + created.id())).body(created);
    }
}
