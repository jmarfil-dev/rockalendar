package com.jmarfildev.rockalendar.support;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author jmarfil
 *
 */
@Component
@Profile("test")
public class DatabaseCleaner {

    private final JdbcTemplate jdbc;

    public DatabaseCleaner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Limpia las tablas para que no haya conflictos entre tests.
     * También restaura el trust_score y banned de los usuarios seed al estado inicial.
     */
    public void truncateMutableTables() {
        jdbc.execute("""
                    TRUNCATE TABLE
                      moderation_actions,
                      event_artists,
                      events,
                      artists
                    RESTART IDENTITY CASCADE
                """);
        // Elimina usuarios creados dinámicamente en tests (mantiene los 3 usuarios seed)
        jdbc.execute("""
                    DELETE FROM users
                    WHERE id NOT IN (
                      'aaaaaaaa-0000-0000-0000-000000000001',
                      'aaaaaaaa-0000-0000-0000-000000000002',
                      'aaaaaaaa-0000-0000-0000-000000000003'
                    )
                """);
        // Restaura banned de los usuarios seed (trust_score ya no se persiste)
        jdbc.execute("UPDATE users SET banned = false");
    }
}
