// Stage 2/3 — service layer
package com.syncboard.service;

import com.syncboard.api.dto.CommentNode;
import com.syncboard.api.dto.CreateCommentRequest;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    List<CommentNode> listComments(UUID ticketId);

    CommentNode createComment(UUID ticketId, CreateCommentRequest request);
}
