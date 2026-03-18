ALTER TABLE users
    ADD COLUMN banned boolean NOT NULL DEFAULT false;

-- Permite scores negativos hasta -200 (ban permanente) para el sistema de confianza
ALTER TABLE users
    DROP CONSTRAINT chk_users_trust_score;

ALTER TABLE users
    ADD CONSTRAINT chk_users_trust_score CHECK (trust_score >= -200);
