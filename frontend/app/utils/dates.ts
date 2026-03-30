/**
 * Formatea un datetime ISO de evento para mostrarlo en la UI.
 * Usa es-ES explícito para garantizar consistencia entre servidor y cliente (evitar hydration mismatch).
 */
export function formatEventDate(isoString: string): string {
  return new Date(isoString).toLocaleString("es-ES", { dateStyle: "short", timeStyle: "short" });
}
