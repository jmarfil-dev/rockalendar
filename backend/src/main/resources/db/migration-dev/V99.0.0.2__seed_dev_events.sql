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
  submitted_at,
  created_by_user_id,
  moderated_at,
  moderated_by_user_id
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
    now(),
    'aaaaaaaa-0000-0000-0000-000000000001',
    now(),
    'aaaaaaaa-0000-0000-0000-000000000002'
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
    now(),
    'aaaaaaaa-0000-0000-0000-000000000003',
    now(),
    'aaaaaaaa-0000-0000-0000-000000000002'
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
    now() - interval '6 days',
    'aaaaaaaa-0000-0000-0000-000000000003',
    NULL,
    NULL
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
    now(),
    'aaaaaaaa-0000-0000-0000-000000000002',
    now(),
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
    now(),
    'aaaaaaaa-0000-0000-0000-000000000003',
    now(),
    'aaaaaaaa-0000-0000-0000-000000000002'
  ),
  (
    'cccccccc-0000-0000-0000-000000000006',
    'Metallica en Valencia',
    'Un concierto de tantos que dan',
    '2026-05-11T22:00:00Z',
    'Plaza de Toros',
    'plaza de toros',
    (SELECT id FROM provinces WHERE name = 'Valencia/València'),
    'València',
    'valencia',
    'APPROVED',
    now(),
    'aaaaaaaa-0000-0000-0000-000000000001',
    now(),
    'aaaaaaaa-0000-0000-0000-000000000002'
  ),
  (
    'cccccccc-0000-0000-0000-000000000007',
    'Rebel Fest',
    'Festival',
    '2026-05-10T22:00:00Z',
    'Sala Andén',
    'sala anden',
    (SELECT id FROM provinces WHERE name = 'Burgos'),
    'Burgos',
    'burgos',
    'PENDING_MODERATION',
    now() - interval '5 days',
    'aaaaaaaa-0000-0000-0000-000000000002',
    NULL,
    NULL
  ),
  (
    'cccccccc-0000-0000-0000-000000000008',
    'Milenrama en Madrid',
    'Concierto de gira mundial',
    '2026-03-15T21:00:00Z',
    'Sala But',
    'sala but',
    (SELECT id FROM provinces WHERE name = 'Madrid'),
    'Madrid',
    'madrid',
    'REJECTED',
    now() - interval '5 days',
    'aaaaaaaa-0000-0000-0000-000000000001',
    now() - interval '3 days',
    'aaaaaaaa-0000-0000-0000-000000000002'
  ),
  (
    'cccccccc-0000-0000-0000-000000000009',
    'Los de Marras en Ávila',
    'Concierto de gira mundial',
    '2026-03-15T21:00:00Z',
    'Polideportivo',
    'polideportivo',
    (SELECT id FROM provinces WHERE name = 'Ávila'),
    'Ávila',
    'avila',
    'HIDDEN',
    now() - interval '4 days',
    'aaaaaaaa-0000-0000-0000-000000000003',
    now() - interval '2 days',
    'aaaaaaaa-0000-0000-0000-000000000001'
  ),
  (
    'cccccccc-0000-0000-0000-000000000010',
    'A Granel en Albacete',
    'Concierto despedida',
    '2026-04-02T21:00:00Z',
    'Recinto Ferial',
    'recinto ferial',
    (SELECT id FROM provinces WHERE name = 'Albacete'),
    'Albacete',
    'albacete',
    'CANCELED',
    now() - interval '3 days',
    'aaaaaaaa-0000-0000-0000-000000000003',
    now() - interval '1 days',
    'aaaaaaaa-0000-0000-0000-000000000002'
  )
;

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
