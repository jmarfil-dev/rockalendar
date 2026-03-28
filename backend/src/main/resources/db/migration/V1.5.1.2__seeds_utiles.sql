-- ============================================================
-- Flyway seeds_producción (v1)
-- Version: 1.1.0.0
-- Description: Script de semillas útiles para PRODUCCIÓN
-- ============================================================

-- Usuario ADMIN principal
INSERT INTO users (id, email, password_hash, "role", trust_score, preferred_language, created_at, privacy_accepted) VALUES
    (gen_random_uuid(), 'cyber.makarra@gmail.com', '$2a$10$QAKdFxX9mBou6b5Hxhc6aeLBNKvXpFxHn1CRzTqNV.ULwYXiQfSrq', 'ADMIN', 100, 'es', now(), true)
;

-- Algnos grupos
INSERT INTO artists (id, "name", slug, created_by_user_id, created_at) VALUES
    (gen_random_uuid(), 'Against You', 'against you', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Vagos Permanentes', 'vagos permanentes', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Brutus'' Daughters', 'brutus daughters', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'No Konforme', 'no konforme', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Manifa', 'manifa', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Orri Berdea', 'orri berdea', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Narco', 'narco', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Dubioza Kolektiv', 'dubioza kolektiv', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Boikot', 'boikot', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Arpaviejas', 'arpaviejas', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Mala Komunikación', 'mala komunikacion', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'El Reno Renardo', 'el reno renardo', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Mamá Ladilla', 'mama ladilla', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Lendakaris Muertos', 'lendakaris muertos', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Loncha Velasco', 'loncha velasco', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Yakovlev 42', 'yakovlev 42', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now()),
    (gen_random_uuid(), 'Mocow Death Brigade', 'mocow death brigade', SELECT id FROM users WHERE email = 'cyber.makarra@gmail.com', now())
;

-- Reglas de blacklist para festivales del grupo Superstruct.
-- El contenido evaluado ya está normalizado (sin tildes, en minúsculas) por SlugNormalizer.removeAccents().
-- "sonar" cubre también "off-sonar" (el guión es límite de palabra para \b).
-- "elrow" cubre "elrow ibiza amnesia", "elrow town" y cualquier variante futura.

INSERT INTO moderation_rules (id, rule_type, value, reason, active, created_at) VALUES
    -- REGEX: términos cortos o con variantes que necesitan límite de palabra
    (gen_random_uuid(), 'REGEX',     '\bsonar\b',           'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'REGEX',     '\bfib\b',             'Festival de Superstruct', true, now()),

    -- TEXT_TERM: términos suficientemente específicos para búsqueda literal
    (gen_random_uuid(), 'TEXT_TERM', 'arenal sound',        'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'o son do camino',     'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'brunch electronik',   'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'morrina fest',        'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'sonorica',            'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'love reggaeton',      'Festival de Superstruct y además es una puta mierda, ¿cómo se te ocurre proponerlo en Rockalendar?', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'madrid salvaje',      'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'caudal fest',         'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'love the twenties',   'Festival de Superstruct y mainstream', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'love the 90s',        'Festival de Superstruct y mainstream', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'elrow',               'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'resurrection fest',   'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'vina rock',           'Festival de Superstruct, mainstream y con prácticas empresariales poco éticas', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'monegros',            'Festival de Superstruct y mainstream', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'brava madrid',        'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'interestelar',        'Festival de Superstruct', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'tsunami',             'Festival de Superstruct y mainstream', true, now()),
    (gen_random_uuid(), 'TEXT_TERM', 'granada sound',       'Festival de Superstruct', true, now())
;
