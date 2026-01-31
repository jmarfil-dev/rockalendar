package com.jmarfildev.rockalendar.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.users.domain.User;
import com.jmarfildev.rockalendar.users.domain.UserRole;
import com.jmarfildev.rockalendar.users.persistence.UserRepository;

/**
 * @author jmarfil
 *
 */
@DataJpaTest
@Import({ AuthUserDetailsService.class, DatabaseCleaner.class })
class AuthUserDetailsServiceTest extends AbstractPostgresTest {

    @Autowired
    AuthUserDetailsService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    @Test
    @DisplayName("loadUserByUsername: email ignore-case -> devuelve UserDetails")
    void loadUser_ignoreCase() {
        userRepository.save(User.builder()
                .email("User@Test.com")
                .passwordHash("{noop}user")
                .role(UserRole.USER.name())
                .build());

        var ud = service.loadUserByUsername("user@test.com");

        assertThat(ud.getUsername()).isEqualTo("User@Test.com");
    }

    @Test
    @DisplayName("loadUserByUsername: si no existe -> UsernameNotFoundException")
    void loadUser_notFound() {
        assertThatThrownBy(() -> service.loadUserByUsername("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("loadUserByUsername: mapea correctamente el role a authority")
    void loadUser_mapsRoleToAuthority() {
        userRepository.save(User.builder()
                .email("user@test.com")
                .passwordHash("{noop}pw")
                .role(UserRole.USER.name())
                .build());

        var ud = service.loadUserByUsername("user@test.com");

        assertThat(ud.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }
}
