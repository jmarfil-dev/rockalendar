CREATE TABLE event_comments (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID         NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    author_user_id  UUID         NULL REFERENCES users(id),
    author_email    TEXT         NOT NULL,
    author_name     TEXT         NULL,
    body            TEXT         NOT NULL CHECK (char_length(body) <= 2000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_event_comments_event_id ON event_comments(event_id);

COMMENT ON TABLE event_comments IS 'Comentarios de usuarios (autenticados o anónimos) sobre eventos, visibles solo para moderadores.';
COMMENT ON COLUMN event_comments.id IS 'Identificador único del comentario.';
COMMENT ON COLUMN event_comments.event_id IS 'Evento al que pertenece el comentario.';
COMMENT ON COLUMN event_comments.author_user_id IS 'Usuario registrado que envió el comentario (NULL si es anónimo).';
COMMENT ON COLUMN event_comments.author_email IS 'Email del autor; tomado del token si autenticado, del formulario si anónimo.';
COMMENT ON COLUMN event_comments.author_name IS 'Nombre visible del autor (opcional).';
COMMENT ON COLUMN event_comments.body IS 'Cuerpo del comentario (máx. 2000 caracteres).';
COMMENT ON COLUMN event_comments.created_at IS 'Fecha y hora de creación.';
