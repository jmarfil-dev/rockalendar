package com.jmarfildev.rockalendar.moderation.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Regla de moderación automática (blacklist de términos, slugs de artistas o patrones regex).
 *
 * @author jmarfil
 */
@Entity
@Table(name = "moderation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationRule {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private ModerationRuleType ruleType;

    @Column(nullable = false, columnDefinition = "text")
    private String value;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
