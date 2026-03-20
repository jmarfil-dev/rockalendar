import type { Artist } from "~/types/artist";

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
  artists: Artist[];
  sourceUrl?: string | null;
};

export type PossibleDuplicateDto = {
  id: string;
  title: string;
  approved: boolean;
};

export type ProposeEventResponse = {
  event: EventPrivateDto;
  possibleDuplicate: PossibleDuplicateDto | null;
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
  artists: Artist[];
  sourceUrl?: string | null;
  status: EventStatus;
  moderationMessage?: string | null;
  createdAt: string;
  submittedAt: string;
};

export const MODERATION_TABS = ["PENDING", "ARCHIVED"] as const;
export type ModerationTab = (typeof MODERATION_TABS)[number];

export type ModerationPendingListItem = {
  id: string;
  title: string;
  submittedAt: string;
  possibleDuplicateOf?: string | null;
};

export type ModerationArchivedListItem = {
  id: string;
  title: string;
  status: EventStatus;
  moderationMessage: string;
  moderatedAt: string;
};

export type InteractionStatus = "INTERESTED" | "GOING";

export type AgendaItem = {
  eventId: string;
  title: string;
  startDateTime: string;
  endDateTime?: string | null;
  venueName: string;
  cityName: string;
  provinceName: string;
  status: InteractionStatus;
  createdAt: string;
};

export type SetInteractionRequest = {
  status: InteractionStatus;
};

export type ModerationApproveRequest = {
  comment?: string;
};

export type ModerationArchiveRequest = {
  reason: string;
};
