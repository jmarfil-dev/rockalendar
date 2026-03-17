export function extractApiError(
  res: { pd: { detail?: string; code?: string } | null },
  t: (key: string) => string,
): string {
  if (res.pd?.detail) return res.pd.detail;
  if (res.pd?.code) return t(res.pd.code); // pd.code ya es la ruta i18n completa
  return t("error.unknown");
}
