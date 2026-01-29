-- ============================================================
-- Flyway provinces (v1)
-- Version: 1.0.0.2
-- Description: Elimina y recrea la tabla provinces. Inserta todas provincias españolas con código INE 01–52. Los UUIDs son deterministas (UUIDv5 basados en ine_code).

-- UUIDv5 namespace para entidades geográficas en Rockalendar
-- IMPORTANTE:
--  - Este namespace nunca debe cambiar.
--  - Los IDs de provincias se generan como:
--      uuid_generate_v5(<namespace>, 'rockalendar:province:<INE_CODE>')
--  - Cambiar este namespace romperá todas las FKs que les hacen referencia.
--
-- Namespace UUID: 4b1c3d3a-9f7b-4f7a-8c2e-0d3d9e6f2a11
-- ============================================================

ALTER TABLE events DROP CONSTRAINT fk_event_province;

DROP TABLE provinces;

CREATE TABLE provinces (
    id uuid PRIMARY KEY,
    ine_code SMALLINT NOT NULL UNIQUE,
    name varchar(80) NOT NULL,
    CONSTRAINT chk_provinces_ine_code CHECK (ine_code BETWEEN 1 AND 52)
);

ALTER TABLE events ADD CONSTRAINT fk_event_province FOREIGN KEY (province_id) REFERENCES provinces(id);

CREATE INDEX IF NOT EXISTS idx_provinces_ine_code ON provinces (ine_code);
CREATE INDEX IF NOT EXISTS idx_events_province_id ON events(province_id);

-- Namespace fijo del proyecto (usar siempre que sea necesario generar UUIDs)
-- rockalendar namespace: 4b1c3d3a-9f7b-4f7a-8c2e-0d3d9e6f2a11
WITH data_provinces(ine_code, name) AS (
    VALUES
        (1,  'Araba/Álava'),
        (2,  'Albacete'),
        (3,  'Alicante/Alacant'),
        (4,  'Almería'),
        (5,  'Ávila'),
        (6,  'Badajoz'),
        (7,  'Illes Balears'),
        (8,  'Barcelona'),
        (9,  'Burgos'),
        (10, 'Cáceres'),
        (11, 'Cádiz'),
        (12, 'Castellón/Castelló'),
        (13, 'Ciudad Real'),
        (14, 'Córdoba'),
        (15, 'A Coruña'),
        (16, 'Cuenca'),
        (17, 'Girona'),
        (18, 'Granada'),
        (19, 'Guadalajara'),
        (20, 'Gipuzkoa'),
        (21, 'Huelva'),
        (22, 'Huesca'),
        (23, 'Jaén'),
        (24, 'León'),
        (25, 'Lleida'),
        (26, 'La Rioja'),
        (27, 'Lugo'),
        (28, 'Madrid'),
        (29, 'Málaga'),
        (30, 'Murcia'),
        (31, 'Navarra'),
        (32, 'Ourense'),
        (33, 'Asturias'),
        (34, 'Palencia'),
        (35, 'Las Palmas'),
        (36, 'Pontevedra'),
        (37, 'Salamanca'),
        (38, 'Santa Cruz de Tenerife'),
        (39, 'Cantabria'),
        (40, 'Segovia'),
        (41, 'Sevilla'),
        (42, 'Soria'),
        (43, 'Tarragona'),
        (44, 'Teruel'),
        (45, 'Toledo'),
        (46, 'Valencia/València'),
        (47, 'Valladolid'),
        (48, 'Bizkaia'),
        (49, 'Zamora'),
        (50, 'Zaragoza'),
        (51, 'Ceuta'),
        (52, 'Melilla')
)
INSERT INTO provinces (id, ine_code, name)
SELECT uuid_generate_v5(
        '4b1c3d3a-9f7b-4f7a-8c2e-0d3d9e6f2a11'::uuid,
        'rockalendar:province:' || lpad(data_provinces.ine_code::TEXT,
        2, '0')
    ) AS id,
    data_provinces.ine_code,
    data_provinces.name
FROM data_provinces
ON CONFLICT (ine_code) DO UPDATE SET name = excluded.name;
