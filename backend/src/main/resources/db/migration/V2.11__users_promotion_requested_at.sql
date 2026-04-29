ALTER TABLE users ADD COLUMN promotion_requested_at TIMESTAMPTZ NULL;

COMMENT ON COLUMN users.promotion_requested_at IS 'Fecha en que el usuario solicitó el ascenso a moderador. NULL si no ha solicitado o si ya fue procesada la solicitud.';
