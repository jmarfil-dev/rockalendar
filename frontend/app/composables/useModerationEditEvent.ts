import type { EventPrivateDto } from "~/types/events";
import type { ArtistChip } from "~/types/artist";
import { ROUTE_PATH } from "~/constants/routes";
import { applyFormErrors } from "~/utils/formErrors";

export const useModerationEditEvent = (eventId: string) => {
  const { t } = useI18n();

  const form = reactive({
    title: "",
    description: "",
    startDate: null as Date | null,
    startTimeUnknown: false,
    endDate: null as Date | null,
    venueName: "",
    provinceId: null as string | null,
    cityName: "",
    artists: [] as ArtistChip[],
    sourceUrl: "",
  });

  const posterFile = ref<File | null>(null);
  const existingPosterUrl = ref<string | null>(null);
  const removePoster = ref(false);
  const loading = ref(true);
  const submitting = ref(false);
  const errorMsg = ref<string | null>(null);
  const fieldErrors = ref<Record<string, string>>({});

  const artistsError = computed(() => {
    const keys = Object.keys(fieldErrors.value);
    const key = keys.find((k) => k === "artists" || k.startsWith("artists["));
    return key ? t(fieldErrors.value[key]!) : null;
  });

  function fillForm(event: EventPrivateDto) {
    form.title = event.title;
    form.description = event.description ?? "";
    form.startDate = new Date(event.startDateTime);
    form.startTimeUnknown = event.startTimeUnknown;
    form.endDate = event.endDate ? new Date(`${event.endDate}T12:00:00`) : null;
    form.venueName = event.venueName;
    form.provinceId = event.provinceId;
    form.cityName = event.cityName;
    form.artists = event.artists.map((a) => ({ name: a.name }));
    form.sourceUrl = event.sourceUrl ?? "";
    existingPosterUrl.value = event.posterUrl ?? null;
  }

  async function load() {
    loading.value = true;
    const res = await fetchAuthResult<EventPrivateDto>(ROUTE_PATH.apiModerationEventDetail(eventId));
    loading.value = false;
    if (res.ok) {
      fillForm(res.data);
      return null;
    }
    return res;
  }

  function resetErrors() {
    errorMsg.value = null;
    fieldErrors.value = {};
  }

  async function submit(): Promise<EventPrivateDto | null> {
    submitting.value = true;
    resetErrors();

    const eventData = {
      title: form.title,
      description: form.description || undefined,
      startDate: form.startDate ? toLocalDateString(form.startDate) : undefined,
      startTime: form.startDate && !form.startTimeUnknown ? toLocalTimeString(form.startDate) : undefined,
      endDate: form.endDate ? toLocalDateString(form.endDate) : undefined,
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
      const res = await fetchAuthResult<EventPrivateDto>(ROUTE_PATH.apiModerationEventDetail(eventId), {
        method: "PUT",
        body: formData,
        query: { removePoster: removePoster.value },
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

  return { form, posterFile, existingPosterUrl, removePoster, loading, submitting, errorMsg, fieldErrors, artistsError, load, submit };
};
