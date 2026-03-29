import type { ProposeEventResponse } from "~/types/events";
import type { ArtistChip } from "~/types/artist";
import { ROUTES } from "~/constants/routes";
import { applyFormErrors } from "~/utils/formErrors";

export const useProposeEvent = () => {
  const { t } = useI18n();

  const form = reactive({
    title: "",
    description: "",
    startDateTime: null as Date | null,
    endDateTime: null as Date | null,
    venueName: "",
    provinceId: null as string | null,
    cityName: "",
    artists: [] as ArtistChip[],
    sourceUrl: "",
  });

  const posterFile = ref<File | null>(null);
  const submitting = ref(false);
  const errorMsg = ref<string | null>(null);
  const fieldErrors = ref<Record<string, string>>({});

  // Devuelve el primer error de artistas (clave artists, artists[0], artists[1]...)
  const artistsError = computed(() => {
    const keys = Object.keys(fieldErrors.value);
    const key = keys.find((k) => k === "artists" || k.startsWith("artists["));
    return key ? t(fieldErrors.value[key]!) : null;
  });

  function resetErrors() {
    errorMsg.value = null;
    fieldErrors.value = {};
  }

  async function submit(): Promise<ProposeEventResponse | null> {
    submitting.value = true;
    resetErrors();

    const eventData = {
      title: form.title,
      description: form.description || undefined,
      startDateTime: form.startDateTime?.toISOString(),
      endDateTime: form.endDateTime?.toISOString() || undefined,
      venueName: form.venueName,
      provinceId: form.provinceId,
      cityName: form.cityName,
      artists: form.artists.map((a) => a.name),
      sourceUrl: normalizeUrl(form.sourceUrl) || undefined,
    };

    const formData = new FormData();
    formData.append("event", new Blob([JSON.stringify(eventData)], { type: "application/json" }));
    if (posterFile.value) {
      formData.append("poster", posterFile.value);
    }

    try {
      const res = await fetchAuthResult<ProposeEventResponse>(ROUTES.apiMeEvents, {
        method: "POST",
        body: formData,
      });

      if (res.ok) {
        clearArtistAutocompleteCache();
        return res.data;
      }

      applyFormErrors(res.pd, t, errorMsg, fieldErrors);
      return null;
    } finally {
      submitting.value = false;
    }
  }

  return { form, posterFile, submitting, errorMsg, fieldErrors, artistsError, submit, resetErrors };
};
