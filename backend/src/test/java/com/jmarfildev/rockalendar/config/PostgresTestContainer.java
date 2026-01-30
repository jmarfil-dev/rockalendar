package com.jmarfildev.rockalendar.config;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Instancia de un Testcontainer Singleton.
 * No se usan @Testcontainers/@Container de forma intencionada porque el "lifecycle per-class" provoca inestabilidad en el datasource con Hikari.
 *
 * @author jmarfil
 *
 */
public final class PostgresTestContainer {

    private PostgresTestContainer() {}

    @SuppressWarnings("resource")
    public static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        INSTANCE.start();
        Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::stop));
    }
}
