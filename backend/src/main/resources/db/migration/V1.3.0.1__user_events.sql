-- Tabla de interacciones de usuario con eventos (agenda personal)
-- Un usuario puede marcar un evento como INTERESTED o GOING
-- La PK compuesta garantiza una sola interacción por par usuario-evento
CREATE TABLE user_events (
    user_id    uuid NOT NULL,
    event_id   uuid NOT NULL,
    status     varchar(15) NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,

    CONSTRAINT pk_user_events        PRIMARY KEY (user_id, event_id),
    CONSTRAINT chk_user_event_status CHECK (status IN ('INTERESTED', 'GOING')),
    CONSTRAINT fk_user_events_user   FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_user_events_event  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Índice para listar la agenda de un usuario eficientemente
CREATE INDEX idx_user_events_user_id ON user_events (user_id);
