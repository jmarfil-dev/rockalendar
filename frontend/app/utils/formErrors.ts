import type { ProblemDetail } from "~/types/api";

export type FormErrors = {
  message: string | null;
  fields: Record<string, string>;
};

export function pdToFormErrors(pd: ProblemDetail | null, t: (key: string) => string): FormErrors {
  if (!pd) {
    return {
      message: t("error.unknown"),
      fields: {},
    };
  }

  const fields = (pd.errors ?? {}) as Record<string, string>;
  let message: string | null = null;

  // Si hay errores en campos ignoramos detail y message
  if (Object.keys(fields).length == 0) {
    if (pd.code) message = t(pd.code);
    else if (pd.detail) message = pd.detail;
    else message = t("error.unknown");
  }
  return { message, fields };
}

export function applyFormErrors(
  pd: ProblemDetail | null,
  t: (key: string) => string,
  messageRef: { value: string | null },
  fieldsRef: { value: Record<string, string> },
) {
  const { message, fields } = pdToFormErrors(pd, t);

  messageRef.value = message;
  fieldsRef.value = fields;
}
