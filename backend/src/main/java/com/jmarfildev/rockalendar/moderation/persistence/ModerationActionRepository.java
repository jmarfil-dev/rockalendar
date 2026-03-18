package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jmarfildev.rockalendar.moderation.domain.ActionType;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;

/**
 * @author jmarfil
 *
 */
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    @Query("SELECT COUNT(a) FROM ModerationAction a WHERE a.eventId = :eventId AND a.action = :action")
    long countByEventIdAndAction(UUID eventId, ActionType action);
}
