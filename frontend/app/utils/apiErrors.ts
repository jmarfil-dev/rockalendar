import type { ProblemDetail } from "~/types/api";

export function extractApiError(
  res: { pd: ProblemDetail | null },
  t: (key: string, params?: Record<string, unknown>) => string,
): string {
  if (res.pd?.code === "error.429.rateLimitExceeded") {
    const seconds = typeof res.pd.retryAfter === "number" ? res.pd.retryAfter : 60;
    return t("error.429.rateLimitExceeded", { seconds });
  }
  if (res.pd?.detail) return res.pd.detail;
  if (res.pd?.code) return t(res.pd.code);
  return t("error.unknown");
}
