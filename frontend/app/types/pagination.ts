export type PageMeta = {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
};

export type PageResponse<T> = {
  content: T[];
  page: PageMeta;
};

export type SortOption = { label: string; value: string };
