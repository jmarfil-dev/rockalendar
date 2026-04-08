export const ROUTES = {
  home: "/",
  events: "/events",
  login: "/login",
  register: "/register",
  forgotPassword: "/forgot-password",
  resetPassword: "/reset-password",

  me: "/me",
  meAgenda: "/me/agenda",
  meEvents: "/me/events",
  meEventPropose: "/me/events/propose",
  meSettings: "/me/settings",

  moderation: "/moderation",
  moderationEvents: "/moderation/events",
  moderationArtists: "/moderation/artists",
  moderationArtistCreate: "/moderation/artists/create",

  admin: "/admin",
  adminSettings: "/admin/settings",
  adminEvents: "/admin/events",

  errorForbidden: "/error/forbidden",

  about: "/about",
  privacy: "/privacy",
  contact: "/contact",

  artists: "/artists",

  // api
  apiLogin: "/api/auth/login",
  apiRegister: "/api/auth/register",
  apiForgotPassword: "/api/auth/forgot-password",
  apiResetPassword: "/api/auth/reset-password",
  apiHome: "/api/events/home",
  apiEvents: "/api/events",
  apiArtists: "/api/artists",
  apiProvincesCombo: "/api/provinces/combo",
  apiMe: "/api/me",
  apiMePromotionRequest: "/api/me/promotion-request",
  apiMePassword: "/api/me/password",
  apiMeLocale: "/api/me/locale",
  apiMeCancelDeletion: "/api/me/cancel-deletion",
  apiMeEvents: "/api/me/events",
  apiMeAgenda: "/api/me/agenda",

  apiModerationEvents: "/api/moderation/events",
  apiContact: "/api/contact",

  apiAdminEvents: "/api/admin/events",

  apiModerationArtists: "/api/moderation/artists",
  apiModerationPending: "/api/moderation/events/pending",
  apiModerationApproved: "/api/moderation/events/approved",
  apiModerationArchived: "/api/moderation/events/archived",
} as const;

export const ROUTE_PATH = {
  eventDetail: (id: string) => `${ROUTES.events}/${id}`,
  artistDetail: (id: string) => `${ROUTES.artists}/${id}`,
  apiArtistDetail: (id: string) => `${ROUTES.apiArtists}/${id}`,
  apiEventDetail: (id: string) => `${ROUTES.apiEvents}/${id}`,
  meEventDetail: (id: string) => `${ROUTES.meEvents}/${id}`,
  apiMeEventDetail: (id: string) => `${ROUTES.apiMeEvents}/${id}`,
  moderationEventDetail: (id: string) => `${ROUTES.moderationEvents}/${id}`,
  apiModerationEventDetail: (id: string) => `${ROUTES.apiModerationEvents}/${id}`,
  apiModerationArtistDetail: (id: string) => `${ROUTES.apiModerationArtists}/${id}`,
  apiModerationApprove: (id: string) => `${ROUTES.apiModerationEvents}/${id}/approve`,
  apiModerationReject: (id: string) => `${ROUTES.apiModerationEvents}/${id}/reject`,
  apiModerationHide: (id: string) => `${ROUTES.apiModerationEvents}/${id}/hide`,
  apiModerationRequestChanges: (id: string) => `${ROUTES.apiModerationEvents}/${id}/request-changes`,
  apiMeAgendaItem: (id: string) => `${ROUTES.apiMeAgenda}/${id}`,
} as const;
