-- ============================================================
-- Flyway columnas acotadas (v1)
-- Version: 1.0.0.3
-- Description: 
-- ============================================================


-- USERS
ALTER TABLE users
  ALTER COLUMN email TYPE varchar(254),
  ALTER COLUMN password_hash TYPE varchar(255),
  ALTER COLUMN "role" TYPE varchar(20),
  ALTER COLUMN preferred_language TYPE varchar(10);
  
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower ON users (lower(email));

ALTER TABLE users
  ADD CONSTRAINT chk_users_trust_score CHECK (trust_score >= 0);


-- EVENTS
ALTER TABLE events
  ALTER COLUMN title TYPE varchar(200),
  ALTER COLUMN venue_name TYPE varchar(200),
  ALTER COLUMN venue_slug TYPE varchar(200),
  ALTER COLUMN city_name TYPE varchar(120),
  ALTER COLUMN city_slug TYPE varchar(120),
  ALTER COLUMN status TYPE varchar(30);

ALTER TABLE events
  ADD CONSTRAINT chk_events_date_range CHECK (end_date_time IS NULL OR end_date_time >= start_date_time);


-- MODERATION_ACTIONS (si decides renombrar)
ALTER TABLE moderation_actions RENAME COLUMN action TO action_type;
ALTER TABLE moderation_actions ALTER COLUMN action_type TYPE varchar(30);
ALTER TABLE moderation_actions ADD CONSTRAINT chk_moderation_action CHECK (action_type IN ('APPROVE','REJECT','HIDE','CANCEL','COMMENT'));

-- PROVINCES y ARTISTS ya están acotadas en scripts anteriores
