-- Los eventos con hora desconocida se almacenaban a medianoche (00:00:00), lo que hacía
-- que desaparecieran de los listados de "próximos eventos" en cuanto empezaba el día.
-- Los movemos al final del día (23:59:59 hora de Madrid) para que sean visibles durante
-- toda la jornada y desaparezcan correctamente cuando esta termina.
UPDATE events
SET start_date_time = (
    date_trunc('day', start_date_time AT TIME ZONE 'Europe/Madrid') + INTERVAL '23:59:00'
) AT TIME ZONE 'Europe/Madrid'
WHERE start_time_unknown = true;
