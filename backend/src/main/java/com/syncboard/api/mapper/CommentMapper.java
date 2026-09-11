// Stage 2 — REST API mappers
package com.syncboard.api.mapper;

import com.syncboard.api.dto.CommentNode;
import com.syncboard.persistence.entity.CommentEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds the threaded {@link CommentNode} tree from a flat list of {@link CommentEntity}
 * rows in one pass: bucket every comment by its parent id, then recursively assemble
 * children starting from the root bucket ({@code null} parent).
 *
 * <p>{@code CommentEntity} exposes {@code getId, getBodyText, getAuthor, getCreatedAt,
 * getEditedAt}, plus {@code getParentComment()} — a lazy self-referential {@code CommentEntity}
 * relation, not a raw {@code UUID} column. Tree assembly below reads {@code getParentComment()
 * .getId()} (or {@code null} for a root comment) to bucket by parent; since {@code flatComments}
 * already holds every comment for the ticket, this needs no extra queries even though the
 * relation is lazy — only the id, already loaded, is touched.
 */
@Component
public class CommentMapper {

    private final UserMapper userMapper;

    public CommentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<CommentNode> toTree(List<CommentEntity> flatComments) {
        Map<UUID, List<CommentEntity>> byParent = new HashMap<>();
        for (CommentEntity comment : flatComments) {
            UUID parentId = comment.getParentComment() == null ? null : comment.getParentComment().getId();
            byParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(comment);
        }
        return buildChildren(null, byParent);
    }

    private List<CommentNode> buildChildren(UUID parentId, Map<UUID, List<CommentEntity>> byParent) {
        List<CommentEntity> children = byParent.getOrDefault(parentId, List.of());
        List<CommentNode> nodes = new ArrayList<>(children.size());
        for (CommentEntity child : children) {
            nodes.add(new CommentNode(
                    child.getId(),
                    child.getBodyText(),
                    userMapper.toSummary(child.getAuthor()),
                    child.getCreatedAt(),
                    child.getEditedAt(),
                    buildChildren(child.getId(), byParent)
            ));
        }
        return nodes;
    }
}
