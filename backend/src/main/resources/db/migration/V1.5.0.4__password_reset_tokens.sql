CREATE TABLE password_reset_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  TEXT        NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prt_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX idx_prt_user_id    ON password_reset_tokens(user_id);

COMMENT ON TABLE password_reset_tokens IS 'Tokens de restablecimiento de contraseña. Se almacena el hash SHA-256 del token, nunca el valor en claro.';
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'SHA-256 hex del token enviado al usuario por email.';
COMMENT ON COLUMN password_reset_tokens.expires_at IS 'Caducidad del token (1 hora desde la emisión).';
COMMENT ON COLUMN password_reset_tokens.used_at    IS 'Momento en que se usó el token. NULL si aún no se ha usado.';
