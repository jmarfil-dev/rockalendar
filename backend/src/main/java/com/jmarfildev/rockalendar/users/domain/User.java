package com.jmarfildev.rockalendar.users.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jmarfil
 *
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; // DB text

    @Column(nullable = false)
    private boolean banned;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "privacy_accepted", nullable = false)
    private boolean privacyAccepted;

    @Column(name = "deletion_requested_at")
    private OffsetDateTime deletionRequestedAt;

    @Column(name = "promotion_requested_at")
    private OffsetDateTime promotionRequestedAt;

    @Column(nullable = false)
    private boolean erased;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UserRole roleEnum() {
        return UserRole.fromDb(role);
    }
}
