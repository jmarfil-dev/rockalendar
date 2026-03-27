const ALLOWED_PROTOCOLS = ["https:", "http:"];

/**
 * Comprueba que una URL tiene un protocolo seguro (http o https).
 * Previene la ejecución de javascript: URIs en atributos href.
 */
export function isSafeUrl(url: string): boolean {
  try {
    return ALLOWED_PROTOCOLS.includes(new URL(url).protocol);
  } catch {
    return false;
  }
}

/**
 * Añade https:// a una URL que no tenga protocolo (ej: "www.ejemplo.com").
 * Devuelve null si la cadena es vacía o solo espacios.
 */
export function normalizeUrl(raw: string): string | null {
  const trimmed = raw.trim();
  if (!trimmed) return null;
  if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
  return "https://" + trimmed;
}
