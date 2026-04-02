/**
 * Formatea un datetime ISO de evento para mostrarlo en la UI.
 * Si startTimeUnknown es true, omite la hora.
 * Usa es-ES explícito para garantizar consistencia entre servidor y cliente (evitar hydration mismatch).
 */
const DATE_OPTS: Intl.DateTimeFormatOptions = {
  day: "2-digit", month: "2-digit", year: "numeric",
  timeZone: "Europe/Madrid",
};

export function formatEventDate(isoString: string, timeUnknown = false): string {
  const opts: Intl.DateTimeFormatOptions = timeUnknown
    ? DATE_OPTS
    : { ...DATE_OPTS, hour: "2-digit", minute: "2-digit" };
  return new Date(isoString).toLocaleString("es-ES", opts);
}

/**
 * Formatea una fecha ISO (YYYY-MM-DD) para mostrarse como fecha de fin de evento.
 * Añade T12:00:00 para evitar desfases de zona horaria al parsear fechas sin hora.
 */
export function formatEventEndDate(isoDate: string): string {
  return new Date(`${isoDate}T12:00:00Z`).toLocaleDateString("es-ES", DATE_OPTS);
}

/**
 * Formatea un datetime ISO como fecha (sin hora). Para fechas de sistema: submittedAt, createdAt, moderatedAt.
 */
export function formatDate(isoString: string): string {
  return new Date(isoString).toLocaleDateString("es-ES", DATE_OPTS);
}

/**
 * Serializa la parte de fecha de un objeto Date usando la hora local del cliente (YYYY-MM-DD).
 * Evita el desfase que produce toISOString(), que convierte a UTC antes de serializar.
 */
export function toLocalDateString(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

/**
 * Serializa la parte de hora de un objeto Date usando la hora local del cliente (HH:mm:ss).
 * Evita el desfase que produce toISOString(), que convierte a UTC antes de serializar.
 */
export function toLocalTimeString(date: Date): string {
  const h = String(date.getHours()).padStart(2, "0");
  const min = String(date.getMinutes()).padStart(2, "0");
  const s = String(date.getSeconds()).padStart(2, "0");
  return `${h}:${min}:${s}`;
}
