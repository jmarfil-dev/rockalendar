package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.moderation.domain.ModerationConfig;

/**
 * @author jmarfil
 */
public interface ModerationConfigRepository extends JpaRepository<ModerationConfig, String> {

    Optional<ModerationConfig> findByKey(String key);
}
