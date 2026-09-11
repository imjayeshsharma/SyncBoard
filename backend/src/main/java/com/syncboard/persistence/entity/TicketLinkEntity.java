// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.entity;

import com.syncboard.domain.LinkPlatform;
import com.syncboard.persistence.converter.LinkPlatformConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA entity for the {@code ticket_links} table (01-CONTRACT.md §3). */
@Entity
@Table(name = "ticket_links")
public class TicketLinkEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "link_title", length = 200)
    private String linkTitle;

    @Convert(converter = LinkPlatformConverter.class)
    @Column(name = "platform", nullable = false, length = 40)
    private LinkPlatform platform;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TicketLinkEntity() {
    }

    public TicketLinkEntity(TicketEntity ticket, String url, String linkTitle, LinkPlatform platform) {
        this.ticket = ticket;
        this.url = url;
        this.linkTitle = linkTitle;
        this.platform = platform;
    }

    @PrePersist
    void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public TicketEntity getTicket() {
        return ticket;
    }

    public void setTicket(TicketEntity ticket) {
        this.ticket = ticket;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public LinkPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(LinkPlatform platform) {
        this.platform = platform;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketLinkEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
