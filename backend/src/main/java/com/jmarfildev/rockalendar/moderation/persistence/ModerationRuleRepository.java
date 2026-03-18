package com.jmarfildev.rockalendar.moderation.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.moderation.domain.ModerationRule;

/**
 * @author jmarfil
 */
public interface ModerationRuleRepository extends JpaRepository<ModerationRule, UUID> {

    List<ModerationRule> findAllByActiveTrue();
}
