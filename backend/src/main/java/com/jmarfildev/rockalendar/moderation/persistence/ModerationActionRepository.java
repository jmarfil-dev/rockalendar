package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;

/**
 * @author jmarfil
 *
 */
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    @Query("SELECT COUNT(a) FROM ModerationAction a WHERE a.eventId = :eventId AND a.action = :action")
    long countByEventIdAndAction(UUID eventId, ActionType action);

    /**
     * Calcula el trust score derivado para un usuario sumando los pesos de todas
     * las acciones de moderación sobre eventos que él creó.
     */
    @Query(value = """
            SELECT COALESCE(SUM(aw.weight), 0)
            FROM moderation_actions ma
            JOIN events e ON e.id = ma.event_id
            JOIN action_weights aw ON aw.action_type = ma.action_type
            WHERE e.created_by_user_id = :userId
            """, nativeQuery = true)
    int sumWeightsForUser(@Param("userId") UUID userId);
}
