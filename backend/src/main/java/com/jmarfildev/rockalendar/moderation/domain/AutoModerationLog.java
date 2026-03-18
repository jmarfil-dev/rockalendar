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
 * Registro de eventos marcados automáticamente por el sistema de moderación.
 * Separado de moderation_actions, que registra acciones humanas.
 *
 * @author jmarfil
 */
@Entity
@Table(name = "auto_moderation_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoModerationLog {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private ModerationRuleType ruleType;

    @Column(name = "matched_value", columnDefinition = "text")
    private String matchedValue;

    @Column(name = "flagged_at", nullable = false)
    private OffsetDateTime flaggedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (flaggedAt == null) {
            flaggedAt = OffsetDateTime.now();
        }
    }
}
