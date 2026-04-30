export type NotificationType =
  | "EVENT_PENDING_MODERATION"
  | "EVENT_FLAGGED"
  | "EVENT_APPROVED"
  | "EVENT_REJECTED"
  | "EVENT_NEEDS_CHANGES"
  | "EVENT_COMMENT"
  | "POSSIBLE_DUPLICATE_DETECTED"
  | "PROMOTION_REQUEST"
  | "USER_AUTOBANNED";

export type NotificationBandeja = "USER" | "MODERATION" | "ADMIN";

export interface Notification {
  id: string;
  type: NotificationType;
  eventId: string | null;
  payload: Record<string, string>;
  isRead: boolean;
  createdAt: string;
}

