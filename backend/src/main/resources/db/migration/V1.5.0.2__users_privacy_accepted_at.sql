ALTER TABLE users
    ADD COLUMN privacy_accepted BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN users.privacy_accepted IS 'El usuario aceptó la política de privacidad en el momento del registro (RGPD). La fecha de aceptación coincide con created_at.';
