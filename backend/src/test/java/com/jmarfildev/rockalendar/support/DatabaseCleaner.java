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
    }
}
