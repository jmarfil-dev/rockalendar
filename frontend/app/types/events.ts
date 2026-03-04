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
