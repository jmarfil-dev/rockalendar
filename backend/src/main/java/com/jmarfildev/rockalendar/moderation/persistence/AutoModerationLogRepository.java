package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.moderation.domain.AutoModerationLog;

/**
 * @author jmarfil
 */
public interface AutoModerationLogRepository extends JpaRepository<AutoModerationLog, UUID> {

    Optional<AutoModerationLog> findByEventId(UUID eventId);
}
