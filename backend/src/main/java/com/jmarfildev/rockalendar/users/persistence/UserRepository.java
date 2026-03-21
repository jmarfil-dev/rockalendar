package com.jmarfildev.rockalendar.users.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.users.domain.User;

/**
 * @author jmarfil
 *
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByDeletionRequestedAtBeforeAndErasedFalse(OffsetDateTime threshold);
}
