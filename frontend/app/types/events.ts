import type { Artist } from "~/types/artist";

export type EventStatus =
  | "PENDING_MODERATION"
  | "FLAGGED"
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
  startTimeUnknown: boolean;
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
  startTimeUnknown: boolean;
  endDate?: string | null;
  provinceName: string;
  cityName: string;
  posterUrl?: string | null;
};

export type EventPublic = {
  id: string;
  title: string;
  description?: string | null;
  startDateTime: string;
  startTimeUnknown: boolean;
  endDate?: string | null;
  venueName: string;
  provinceId: string;
  provinceName: string;
  cityName: string;
  artists: Artist[];
  sourceUrl?: string | null;
  ticketUrl?: string | null;
  posterUrl?: string | null;
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
  startTimeUnknown: boolean;
  endDate?: string | null;
  venueName: string;
  provinceId: string;
  provinceName: string;
  cityName: string;
  artists: Artist[];
  sourceUrl?: string | null;
  ticketUrl?: string | null;
  posterUrl?: string | null;
  status: EventStatus;
  flagged: boolean;
  moderationMessage?: string | null;
  createdAt: string;
  submittedAt: string;
};

export type ModerationRuleType = "TEXT_TERM" | "ARTIST_SLUG" | "REGEX" | "SPAM";

export type FlagInfoDto = {
  ruleType: ModerationRuleType;
  reason: string;
  matchedValue?: string | null;
};

export type ModerationEventDetailResponse = {
  event: EventPrivateDto;
  flagInfo?: FlagInfoDto | null;
  possibleDuplicateOf?: string | null;
};

export const MODERATION_TABS = ["PENDING", "APPROVED", "ARCHIVED"] as const;
export type ModerationTab = (typeof MODERATION_TABS)[number];

export type ModerationPendingListItem = {
  id: string;
  title: string;
  submittedAt: string;
  possibleDuplicateOf?: string | null;
  status: EventStatus;
};

export type ModerationApprovedListItem = {
  id: string;
  title: string;
  approvedAt: string;
};

export type ModerationArchivedListItem = {
  id: string;
  title: string;
  status: EventStatus;
  moderationMessage: string;
  moderatedAt: string;
};

export type AdminEventListItem = {
  id: string;
  title: string;
  startDateTime: string;
  startTimeUnknown: boolean;
  provinceName: string;
  status: EventStatus;
};

export type InteractionStatus = "INTERESTED" | "GOING";

export type AgendaItem = {
  eventId: string;
  title: string;
  startDateTime: string;
  startTimeUnknown: boolean;
  endDate?: string | null;
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

export type ScrapeEventPosterResponse = {
  posterUrl: string;
  posterKey: string;
};
