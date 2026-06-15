export type LoginRequest = { email: string; password: string };
export type RegisterRequest = LoginRequest & { privacyAccepted: boolean; locale?: string; website?: string };
export type AuthSessionResponse = { expiresAt: string };
