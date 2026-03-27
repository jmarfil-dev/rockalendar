-- Corrige el error de diseño original: created_by_user_id nunca debió admitir nulos.

ALTER TABLE events  ALTER COLUMN created_by_user_id SET NOT NULL;
ALTER TABLE artists ALTER COLUMN created_by_user_id SET NOT NULL;
