ALTER TABLE users
    ADD COLUMN deletion_requested_at TIMESTAMPTZ NULL;

ALTER TABLE users
    ADD COLUMN erased BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN users.deletion_requested_at IS 'Fecha en que el usuario solicitó la eliminación de su cuenta. NULL si no hay solicitud activa. La cuenta se anonimiza definitivamente 7 días después.';
COMMENT ON COLUMN users.erased IS 'True cuando la cuenta ha sido anonimizada definitivamente tras el período de gracia de 7 días.';
