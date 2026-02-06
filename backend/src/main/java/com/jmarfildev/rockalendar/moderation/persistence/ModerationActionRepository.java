package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;

/**
 * @author jmarfil
 *
 */
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {}
