-- Referencia al evento que podría ser duplicado del actual.
-- Nulo si no se detectó ningún duplicado al proponer.
ALTER TABLE events ADD COLUMN possible_duplicate_of UUID REFERENCES events(id);
