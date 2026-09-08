// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.entity;

import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;
import com.syncboard.persistence.converter.PriorityConverter;
import com.syncboard.persistence.converter.TicketStatusConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity for the {@code tickets} table (01-CONTRACT.md §3). No business logic lives here —
 * legal status transitions and their side effects (assignee/reason requirements, etc.) are
 * enforced by the service layer (A3), per 01-CONTRACT.md §2.
 */
@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description")
    private String description;

    @Convert(converter = TicketStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Convert(converter = PriorityConverter.class)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority;

    @Column(name = "category", length = 80)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assignee;

    @Convert(converter = TicketStatusConverter.class)
    @Column(name = "blocked_from_status", length = 20)
    private TicketStatus blockedFromStatus;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketLinkEntity> links = new ArrayList<>();

    @OneToMany(mappedBy = "ticket")
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket")
    private List<TicketStatusHistoryEntity> history = new ArrayList<>();

    public TicketEntity() {
    }

    @PrePersist
    void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UserEntity getReporter() {
        return reporter;
    }

    public void setReporter(UserEntity reporter) {
        this.reporter = reporter;
    }

    public UserEntity getAssignee() {
        return assignee;
    }

    public void setAssignee(UserEntity assignee) {
        this.assignee = assignee;
    }

    public TicketStatus getBlockedFromStatus() {
        return blockedFromStatus;
    }

    public void setBlockedFromStatus(TicketStatus blockedFromStatus) {
        this.blockedFromStatus = blockedFromStatus;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public long getVersion() {
        return version;
    }

    public List<TicketLinkEntity> getLinks() {
        return links;
    }

    public List<CommentEntity> getComments() {
        return comments;
    }

    public List<TicketStatusHistoryEntity> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
