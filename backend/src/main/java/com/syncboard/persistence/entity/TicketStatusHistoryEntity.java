// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.entity;

import com.syncboard.domain.TicketStatus;
import com.syncboard.persistence.converter.TicketStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code ticket_status_history} table (01-CONTRACT.md §3). This is an
 * append-only audit trail: {@code @Immutable} tells Hibernate to never issue an UPDATE for
 * loaded instances, there are no setters, and {@link com.syncboard.persistence.repository.TicketStatusHistoryRepository}
 * deliberately exposes no update/delete methods. Rows are created exclusively via
 * {@link #create}.
 */
@Entity
@Table(name = "ticket_status_history")
@Immutable
public class TicketStatusHistoryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private TicketEntity ticket;

    @Convert(converter = TicketStatusConverter.class)
    @Column(name = "from_status", length = 20, updatable = false)
    private TicketStatus fromStatus;

    @Convert(converter = TicketStatusConverter.class)
    @Column(name = "to_status", nullable = false, length = 20, updatable = false)
    private TicketStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false, updatable = false)
    private UserEntity changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(name = "note", updatable = false)
    private String note;

    /*
     * Fields are deliberately non-final: Hibernate needs a no-arg constructor plus reflective
     * field access to materialize rows loaded from the database. Immutability after construction
     * is enforced instead by (a) never exposing a setter and (b) @Immutable, which tells
     * Hibernate to never issue an UPDATE for a managed instance of this entity.
     */

    /** Hibernate reflective-access constructor only — do not call directly. */
    protected TicketStatusHistoryEntity() {
    }

    /** Package-private: all-args construction happens only via {@link #create}. */
    TicketStatusHistoryEntity(
            UUID id,
            TicketEntity ticket,
            TicketStatus fromStatus,
            TicketStatus toStatus,
            UserEntity changedBy,
            Instant changedAt,
            String note) {
        this.id = id;
        this.ticket = ticket;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.note = note;
    }

    /**
     * Creates one immutable audit row. {@code changedAt} is supplied by the caller (typically
     * {@code Instant.now(clock)} using the {@link com.syncboard.persistence.PersistenceConfig}
     * {@code Clock} bean) rather than defaulted here, since this package owns persistence only,
     * not the transition business logic (that's A3, per 01-CONTRACT.md §2).
     */
    public static TicketStatusHistoryEntity create(
            TicketEntity ticket,
            TicketStatus fromStatus,
            TicketStatus toStatus,
            UserEntity changedBy,
            Instant changedAt,
            String note) {
        return new TicketStatusHistoryEntity(
                UUID.randomUUID(), ticket, fromStatus, toStatus, changedBy, changedAt, note);
    }

    public UUID getId() {
        return id;
    }

    public TicketEntity getTicket() {
        return ticket;
    }

    public TicketStatus getFromStatus() {
        return fromStatus;
    }

    public TicketStatus getToStatus() {
        return toStatus;
    }

    public UserEntity getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketStatusHistoryEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
