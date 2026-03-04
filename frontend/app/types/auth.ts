export type LoginRequest = { email: string; password: string };
export type AuthTokenResponse = { accessToken: string; expiresAt: string }; // Instant => ISO string
