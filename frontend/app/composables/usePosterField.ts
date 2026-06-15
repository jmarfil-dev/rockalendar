import type { ScrapeEventPosterResponse } from "~/types/events";
import { ROUTES } from "~/constants/routes";

export type PosterMode = "file" | "url";

const POSTER_MAX_MB = 5;
const POSTER_ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"];

export const usePosterField = () => {
  const { t } = useI18n();

  const mode = ref<PosterMode>("file");

  // File mode
  const posterFile = ref<File | null>(null);
  const posterFileError = ref<string | null>(null);
  const filePreviewUrl = computed(() =>
    posterFile.value ? URL.createObjectURL(posterFile.value) : null,
  );

  // URL mode
  const urlInput = ref("");
  const importedPosterUrl = ref<string | null>(null);
  const importedPosterKey = ref<string | null>(null);
  const isImporting = ref(false);
  const importError = ref<string | null>(null);

  const previewUrl = computed<string | null>(() =>
    mode.value === "file" ? filePreviewUrl.value : importedPosterUrl.value,
  );

  const hasPoster = computed(() =>
    mode.value === "file" ? !!posterFile.value : !!importedPosterUrl.value,
  );

  // Confirmación de cambio de modo cuando ya hay imagen cargada
  const pendingMode = ref<PosterMode | null>(null);
  const showModeConfirm = ref(false);

  function requestModeSwitch(newMode: PosterMode) {
    if (newMode === mode.value) return;
    if (hasPoster.value) {
      pendingMode.value = newMode;
      showModeConfirm.value = true;
    } else {
      applyModeSwitch(newMode);
    }
  }

  function confirmModeSwitch() {
    if (!pendingMode.value) return;
    if (mode.value === "url" && importedPosterKey.value) {
      tryDeleteScrapedPoster(importedPosterKey.value);
    }
    applyModeSwitch(pendingMode.value);
    pendingMode.value = null;
    showModeConfirm.value = false;
  }

  function cancelModeSwitch() {
    pendingMode.value = null;
    showModeConfirm.value = false;
  }

  function applyModeSwitch(newMode: PosterMode) {
    mode.value = newMode;
    posterFile.value = null;
    posterFileError.value = null;
    urlInput.value = "";
    importedPosterUrl.value = null;
    importedPosterKey.value = null;
    importError.value = null;
  }

  function setPosterFile(file: File) {
    posterFileError.value = null;
    if (!POSTER_ALLOWED_TYPES.includes(file.type)) {
      posterFileError.value = t("events.posterInvalidType");
      return false;
    }
    if (file.size > POSTER_MAX_MB * 1024 * 1024) {
      posterFileError.value = t("events.posterTooLarge", { max: POSTER_MAX_MB });
      return false;
    }
    posterFile.value = file;
    return true;
  }

  function clearPoster() {
    if (mode.value === "url" && importedPosterKey.value) {
      tryDeleteScrapedPoster(importedPosterKey.value);
    }
    posterFile.value = null;
    posterFileError.value = null;
    urlInput.value = "";
    importedPosterUrl.value = null;
    importedPosterKey.value = null;
    importError.value = null;
  }

  async function importFromUrl() {
    const url = urlInput.value.trim();
    if (!url || isImporting.value) return;

    // Si ya había una imagen importada, limpiar el objeto antes de importar la nueva
    if (importedPosterKey.value) {
      tryDeleteScrapedPoster(importedPosterKey.value);
      importedPosterUrl.value = null;
      importedPosterKey.value = null;
    }

    isImporting.value = true;
    importError.value = null;

    const res = await fetchAuthResult<ScrapeEventPosterResponse>(ROUTES.apiMeEventScrapePoster, {
      method: "POST",
      body: { sourceUrl: url },
    });

    isImporting.value = false;

    if (res.ok) {
      importedPosterUrl.value = res.data.posterUrl;
      importedPosterKey.value = res.data.posterKey;
    } else {
      importError.value = resolveImportError(res.pd?.code);
    }
  }

  function resolveImportError(code: string | undefined | null): string {
    switch (code) {
      case "error.422.scrapeFacebookBlocked":
        return t("events.posterImportFacebookBlocked");
      case "error.422.scrapeNoOgImage":
        return t("events.posterImportNoImage");
      case "error.422.scrapeUnreachable":
        return t("events.posterImportUnreachable");
      case "error.422.scrapeInvalidUrl":
        return t("events.posterImportInvalidUrl");
      default:
        return t("events.posterImportError");
    }
  }

  function tryDeleteScrapedPoster(key: string) {
    fetchAuthResult(ROUTES.apiMeEventScrapePoster, {
      method: "DELETE",
      query: { key },
    }).catch(() => {});
  }

  return {
    mode,
    posterFile,
    posterFileError,
    urlInput,
    importedPosterUrl,
    importedPosterKey,
    isImporting,
    importError,
    previewUrl,
    hasPoster,
    pendingMode,
    showModeConfirm,
    requestModeSwitch,
    confirmModeSwitch,
    cancelModeSwitch,
    setPosterFile,
    clearPoster,
    importFromUrl,
  };
};
