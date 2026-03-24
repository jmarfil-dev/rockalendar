ALTER TABLE events
    ADD COLUMN poster_url VARCHAR(500) NULL,
    ADD COLUMN poster_key VARCHAR(300) NULL;

COMMENT ON COLUMN events.poster_url IS 'URL pública del cartel del evento, servida desde el almacenamiento de objetos.';
COMMENT ON COLUMN events.poster_key IS 'Clave del objeto en el bucket S3 (p. ej. posters/{eventId}/{uuid}.jpg). Necesaria para borrar el fichero.';
