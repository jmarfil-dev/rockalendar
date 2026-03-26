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
