const SORT_FIELD_I18N_KEY = {
  date: "dates.date",
  title: "events.title",
  province: "geo.province",
  city: "geo.city",
  status: "events.status",
} as const;

export type SortField = keyof typeof SORT_FIELD_I18N_KEY;

export function useSortOptions(fields: SortField[]) {
  const { t } = useI18n();
  return computed(() => fields.map((f) => ({ label: t(SORT_FIELD_I18N_KEY[f]), value: f })));
}
