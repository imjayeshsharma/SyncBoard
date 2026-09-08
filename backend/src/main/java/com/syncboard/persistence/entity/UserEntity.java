// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity for the {@code users} table (01-CONTRACT.md §3).
 * A user is either an internal Google Workspace principal (reporter/assignee/comment author)
 * seeded via Flyway or created on first OIDC sign-in (Stage 5, owned by A4/A3).
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "google_subject", nullable = false, unique = true, length = 255)
    private String googleSubject;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "department", length = 120)
    private String department;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA-only constructor. */
    protected UserEntity() {
    }

    public UserEntity(String googleSubject, String email, String fullName, String department) {
        this.googleSubject = Objects.requireNonNull(googleSubject, "googleSubject");
        this.email = Objects.requireNonNull(email, "email");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.department = department;
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

    public String getGoogleSubject() {
        return googleSubject;
    }

    public void setGoogleSubject(String googleSubject) {
        this.googleSubject = googleSubject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
