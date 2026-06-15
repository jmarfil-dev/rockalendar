ALTER TABLE events ADD COLUMN ticket_url TEXT NULL;

COMMENT ON COLUMN events.ticket_url IS 'Enlace de venta de entradas.';
