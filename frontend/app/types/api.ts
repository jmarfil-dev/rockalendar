export type ProblemDetail = {
  status?: number;
  title?: string;
  detail?: string;
  type?: string;
  code?: string;
  errors?: Record<string, string>;
  timestamp?: string;
  [k: string]: unknown;
};

export type ApiResult<T> =
  | { ok: true; data: T }
  | { ok: false; status: number; pd: ProblemDetail | null; raw: unknown };
