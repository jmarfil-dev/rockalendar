/**
 * Formatea un datetime ISO de evento para mostrarlo en la UI.
 * Usa la localización del navegador.
 */
export function formatEventDate(isoString: string): string {
  return new Date(isoString).toLocaleString();
}
