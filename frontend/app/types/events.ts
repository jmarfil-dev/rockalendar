export type EventStatus =
  | "PENDING_MODERATION"
  | "APPROVED"
  | "REJECTED"
  | "DRAFT"
  | "NEEDS_CHANGES"
  | "HIDDEN"
  | "CANCELED"
  | "ERASED";

export const ME_EVENT_TABS = ["PENDING", "CHANGES", "OTHERS", "ALL"] as const;
export type MeEventTab = (typeof ME_EVENT_TABS)[number];

export type EventPrivateListItem = {
  id: string;
  title: string;
  startDateTime: string;
  provinceName: string;
  cityName: string;
  status: EventStatus;
  moderationMessage?: string | null;
  submittedAt: string;
};

export type EventPublicListItem = {
  // Mismos campos que EventPublicListItemDto
  id: string;
  title: string;
  startDateTime: string;
  endDateTime?: string;
  provinceName: string;
  cityName: string;
};

export type EventPublic = {
  id: string;
  title: string;
  description?: string | null;
  startDateTime: string;
  endDateTime?: string | null;
  venueName: string;
  provinceId: string;
  provinceName: string;
  cityName: string;
  artists: string[];
  sourceUrl?: string | null;
};

export type EventPrivateDto = {
  id: string;
  title: string;
  description?: string | null;
  startDateTime: string;
  endDateTime?: string | null;
  venueName: string;
  provinceId: string;
  provinceName: string;
  cityName: string;
  artists: string[];
  sourceUrl?: string | null;
  status: EventStatus;
  moderationMessage?: string | null;
  createdAt: string;
  submittedAt: string;
};
