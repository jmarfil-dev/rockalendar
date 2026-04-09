-- ============================================================
-- Seed users
-- ============================================================
INSERT INTO users (id, email, password_hash, role)
VALUES
  (
    'aaaaaaaa-0000-0000-0000-000000000001',
    'admin@rockalendar.local',
    '$2a$10$9xbPeHEmXmhpo3GjPWNsHujR6X88wBDVLmmR3vmsPxmFogJR5q2nC', -- Test@1234
    'ADMIN'
  ),
  (
    'aaaaaaaa-0000-0000-0000-000000000002',
    'moderator@rockalendar.local',
    '$2a$10$9xbPeHEmXmhpo3GjPWNsHujR6X88wBDVLmmR3vmsPxmFogJR5q2nC', -- Test@1234
    'MODERATOR'
  ),
  (
    'aaaaaaaa-0000-0000-0000-000000000003',
    'user@rockalendar.local',
    '$2a$10$9xbPeHEmXmhpo3GjPWNsHujR6X88wBDVLmmR3vmsPxmFogJR5q2nC', -- Test@1234
    'USER'
  )
;
