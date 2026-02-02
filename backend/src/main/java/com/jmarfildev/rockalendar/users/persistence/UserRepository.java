package com.jmarfildev.rockalendar.users.persistence;

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
}
