export type LoginRequest = { email: string; password: string };
export type RegisterRequest = LoginRequest & { privacyAccepted: boolean };
export type AuthTokenResponse = { accessToken: string; expiresAt: string }; // Instant => ISO string

export type JwtPayload = {
  sub?: string;
  exp?: number;
  iat?: number;
  roles?: string[];
  [k: string]: unknown;
};
