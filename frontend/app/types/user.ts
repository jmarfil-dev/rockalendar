export type MeDto = {
  id: string;
  email: string;
  role: string;
  preferredLanguage: string | null;
  promotionEligible: boolean;
  deletionRequestedAt: string | null;
};
