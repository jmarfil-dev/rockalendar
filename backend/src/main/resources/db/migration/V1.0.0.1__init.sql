-- ============================================================
-- Flyway init schema (v1)
-- Version: 1.0.0.1
-- Description: Initial schema for Rockalendar MVP
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    "role" TEXT NOT NULL,
    trust_score INT NOT NULL DEFAULT 0,
    preferred_language TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_roles CHECK ("role" IN ('USER', 'MODERATOR', 'ADMIN'))
);


-- ============================================================
-- PROVINCES
-- ============================================================
CREATE TABLE provinces (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);


-- ============================================================
-- ARTISTS
-- ============================================================
CREATE TABLE artists (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    CONSTRAINT uk_artist_slug UNIQUE (slug)
);

-- Helpful index for slug search (optional)
CREATE INDEX idx_artists_name_lower ON artists (lower(name));


-- ============================================================
-- EVENTS
-- ============================================================
CREATE TABLE events (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NULL,
    start_date_time TIMESTAMPTZ NOT NULL,
    end_date_time TIMESTAMPTZ NULL,
    venue_name TEXT NOT NULL,
    venue_slug TEXT NOT NULL,
    province_id UUID NOT NULL,
    city_name TEXT NOT NULL,
    city_slug TEXT NOT NULL,
    status TEXT NOT NULL,
    source_url TEXT NULL,
    created_by_user_id UUID NULL,
    approved_by_user_id UUID NULL,
    rejection_reason TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_event_province FOREIGN KEY (province_id) REFERENCES provinces(id),
    CONSTRAINT fk_event_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_event_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id),
    CONSTRAINT chk_events_status CHECK (status IN ('PENDING_MODERATION', 'APPROVED', 'REJECTED', 'CANCELLED', 'HIDDEN'))
);

-- Indexes for common filters
CREATE INDEX idx_events_status_start_date ON events (status, start_date_time);
CREATE INDEX idx_events_province_city_start ON events (province_id, city_slug, start_date_time);
CREATE INDEX idx_events_city_slug ON events (city_slug);
CREATE INDEX idx_events_venue_slug ON events (venue_slug);


-- ============================================================
-- EVENT ↔ ARTIST (N:M)
-- ============================================================
CREATE TABLE event_artists (
    event_id UUID NOT NULL,
    artist_id UUID NOT NULL,
    CONSTRAINT pk_event_artists PRIMARY KEY (event_id, artist_id),
    CONSTRAINT fk_event_artists_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_artists_artist FOREIGN KEY (artist_id) REFERENCES artists(id)
);


-- ============================================================
-- MODERATION AUDIT
-- ============================================================
CREATE TABLE moderation_actions (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    moderator_id UUID NOT NULL,
    ACTION TEXT NOT NULL,
    reason TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_moderation_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_moderation_moderator FOREIGN KEY (moderator_id) REFERENCES users(id)
);
