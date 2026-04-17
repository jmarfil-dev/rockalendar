-- ============================================================
-- Tabla de notificaciones in-app (fan-out en escritura)
-- Una fila por destinatario; sin tabla de lecturas separada.
-- Version: 2.9
-- ============================================================

CREATE TABLE notifications (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    type          TEXT        NOT NULL,
    recipient_id  UUID        NOT NULL REFERENCES users(id),
    event_id      UUID        NULL REFERENCES events(id) ON DELETE SET NULL,
    payload       JSONB       NOT NULL DEFAULT '{}',
    is_read       BOOLEAN     NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índice parcial para consultar no leídas de un usuario sin escanear las ya leídas
CREATE INDEX idx_notifications_recipient_unread ON notifications(recipient_id) WHERE is_read = false;

COMMENT ON TABLE notifications IS 'Notificaciones in-app. Una fila por destinatario (fan-out en escritura).';
COMMENT ON COLUMN notifications.id           IS 'Identificador único de la notificación.';
COMMENT ON COLUMN notifications.type         IS 'Tipo de notificación (enum NotificationType).';
COMMENT ON COLUMN notifications.recipient_id IS 'Usuario destinatario.';
COMMENT ON COLUMN notifications.event_id     IS 'Evento relacionado; NULL si no aplica. Se pone a NULL si el evento se borra.';
COMMENT ON COLUMN notifications.payload      IS 'Datos adicionales de la notificación en formato JSONB.';
COMMENT ON COLUMN notifications.is_read      IS 'true cuando el usuario ha marcado la notificación como leída.';
COMMENT ON COLUMN notifications.created_at   IS 'Momento en que se creó la notificación.';
