package com.jmarfildev.rockalendar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import jakarta.persistence.EntityManager;

/**
 * Clase abstracta que crea la configuración de los Test Containers para heredarla en los Tests.
 *
 * @author jmarfil
 *
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class AbstractPostgresTest {

    @Autowired
    EntityManager em;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        var pg = PostgresTestContainer.INSTANCE;

        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
