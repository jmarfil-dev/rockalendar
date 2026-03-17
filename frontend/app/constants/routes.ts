export const ROUTES = {
  home: "/",
  events: "/events",
  login: "/login",
  register: "/register",

  me: "/me",
  meAgenda: "/me/agenda",
  meEvents: "/me/events",
  meEventPropose: "/me/events/propose",

  moderation: "/moderation",
  admin: "/admin",

  errorForbidden: "/error/forbidden",

  // api
  apiLogin: "/api/auth/login",
  apiRegister: "/api/auth/register",
  apiHome: "/api/events/home",
  apiEvents: "/api/events",
  apiArtists: "/api/artists",
  apiProvincesCombo: "/api/provinces/combo",
  apiMeEvents: "/api/me/events",
} as const;

export const ROUTE_PATH = {
  eventDetail: (id: string) => `${ROUTES.events}/${id}`,
  apiEventDetail: (id: string) => `${ROUTES.apiEvents}/${id}`,
  artistDetail: (id: string) => `${ROUTES.apiArtists}/${id}`,
  apiArtistDetail: (id: string) => `${ROUTES.apiArtists}/${id}`,
  meEventDetail: (id: string) => `${ROUTES.meEvents}/${id}`,
  apiMeEventDetail: (id: string) => `${ROUTES.apiMeEvents}/${id}`,
} as const;
