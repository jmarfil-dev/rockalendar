-- ============================================================
-- Seed users
-- ============================================================
INSERT INTO users (id, email, password_hash, role, trust_score)
VALUES
  (
    'aaaaaaaa-0000-0000-0000-000000000001',
    'admin@rockalendar.local',
    '$2a$10$PdHjxglteAXRNln8TXtwRO3/5x/GSdptWktsoZpYZGeWiNDAQYgUe', -- test1234
    'ADMIN',
    100
  ),
  (
    'aaaaaaaa-0000-0000-0000-000000000002',
    'moderator@rockalendar.local',
    '$2a$10$PdHjxglteAXRNln8TXtwRO3/5x/GSdptWktsoZpYZGeWiNDAQYgUe', -- test1234
    'MODERATOR',
    80
  ),
  (
    'aaaaaaaa-0000-0000-0000-000000000003',
    'user@rockalendar.local',
    '$2a$10$PdHjxglteAXRNln8TXtwRO3/5x/GSdptWktsoZpYZGeWiNDAQYgUe', -- test1234
    'USER',
    10
  )
;
