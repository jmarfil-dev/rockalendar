export type I18nLike = {
  locale: { value: string };
};

export type AppLocale = "en" | "es";

export type LocaleOption = {
  value: AppLocale;
  label: string;
  flagSrc: string;
};
