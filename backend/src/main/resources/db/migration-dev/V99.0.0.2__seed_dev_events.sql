-- ============================================================
-- Seed artists
-- ============================================================
INSERT INTO artists (id, name, slug) VALUES
  ('bbbbbbbb-0000-0000-0000-000000000001', 'A Granel', 'a granel'),
  ('bbbbbbbb-0000-0000-0000-000000000002', 'Milenrama', 'milenrama'),
  ('bbbbbbbb-0000-0000-0000-000000000003', 'Los de Marras', 'los de marras'),
  ('bbbbbbbb-0000-0000-0000-000000000004', 'Ska-P', 'ska p'),
  ('bbbbbbbb-0000-0000-0000-000000000005', 'Metallica', 'metallica')
ON CONFLICT (slug) DO NOTHING;

-- ============================================================
-- Seed events
-- ============================================================
INSERT INTO events (
  id,
  title,
  description,
  start_date_time,
  venue_name,
  venue_slug,
  province_id,
  city_name,
  city_slug,
  status,
  created_by_user_id
) VALUES
  (
    'cccccccc-0000-0000-0000-000000000001',
    'Ska-P en Madrid',
    'Concierto de gira mundial',
    '2026-03-15T21:00:00Z',
    'WiZink Center',
    'wizink center',
    (SELECT id FROM provinces WHERE name = 'Madrid'),
    'Madrid',
    'madrid',
    'APPROVED',
    'aaaaaaaa-0000-0000-0000-000000000001'
  ),
  (
    'cccccccc-0000-0000-0000-000000000005',
    'Ska-P en Madrid Bis',
    'Concierto de gira mundial',
    '2026-03-15T21:00:00Z',
    'Vistalegre',
    'vistalegre',
    (SELECT id FROM provinces WHERE name = 'Madrid'),
    'Madrid',
    'madrid',
    'APPROVED',
    'aaaaaaaa-0000-0000-0000-000000000001'
  ),
  (
    'cccccccc-0000-0000-0000-000000000002',
    'Milenrama en Barcelona',
    'Concierto despedida',
    '2026-04-02T21:00:00Z',
    'Palau Sant Jordi',
    'palau sant jordi',
    (SELECT id FROM provinces WHERE name = 'Barcelona'),
    'Barcelona',
    'barcelona',
    'APPROVED',
    'aaaaaaaa-0000-0000-0000-000000000003'
  ),
  (
    'cccccccc-0000-0000-0000-000000000003',
    'Los de Marras en Valencia',
    'Un concierto de tantos que dan',
    '2026-05-10T22:00:00Z',
    'Plaza de Toros',
    'plaza de toros',
    (SELECT id FROM provinces WHERE name = 'Valencia/València'),
    'València',
    'valencia',
    'PENDING_MODERATION',
    'aaaaaaaa-0000-0000-0000-000000000003'
  ),
  (
    'cccccccc-0000-0000-0000-000000000004',
    'A Granel en Valencia',
    'Un concierto de tantos que dan',
    '2026-05-11T22:00:00Z',
    'Plaza de Toros',
    'plaza de toros',
    (SELECT id FROM provinces WHERE name = 'Valencia/València'),
    'València',
    'valencia',
    'APPROVED',
    'aaaaaaaa-0000-0000-0000-000000000001'
  ),
  (
    'cccccccc-0000-0000-0000-000000000006',
    'Metallica en Barcelona',
    'Un concierto de tantos que dan',
    '2026-05-11T22:00:00Z',
    'Plaza de Toros',
    'plaza de toros',
    (SELECT id FROM provinces WHERE name = 'Barcelona'),
    'València',
    'valencia',
    'APPROVED',
    'aaaaaaaa-0000-0000-0000-000000000001'
  );

-- ============================================================
-- Event ↔ Artist relations
-- ============================================================
INSERT INTO event_artists (event_id, artist_id) VALUES
  ('cccccccc-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000004'),
  ('cccccccc-0000-0000-0000-000000000002', 'bbbbbbbb-0000-0000-0000-000000000002'),
  ('cccccccc-0000-0000-0000-000000000003', 'bbbbbbbb-0000-0000-0000-000000000003'),
  ('cccccccc-0000-0000-0000-000000000004', 'bbbbbbbb-0000-0000-0000-000000000001'),
  ('cccccccc-0000-0000-0000-000000000005', 'bbbbbbbb-0000-0000-0000-000000000004'),
  ('cccccccc-0000-0000-0000-000000000006', 'bbbbbbbb-0000-0000-0000-000000000005')
ON CONFLICT DO NOTHING;
