import { ROUTES } from "~/constants/routes";
import type { MeDto } from "~/types/user";

export function useMe() {
  const { t } = useI18n();
  const toast = useToast();

  const me = ref<MeDto | null>(null);
  const loading = ref(false);
  const promoting = ref(false);

  async function fetchMe() {
    loading.value = true;
    const result = await fetchAuthResult<MeDto>(ROUTES.apiMe);
    loading.value = false;

    if (result.ok) {
      me.value = result.data;
    }
  }

  async function requestPromotion() {
    promoting.value = true;
    const result = await fetchAuthResult<MeDto>(ROUTES.apiMePromotionRequest, { method: "POST" });
    promoting.value = false;

    if (result.ok) {
      me.value = result.data;
      toast.add({
        severity: "success",
        summary: t("me.promotion.successTitle"),
        life: 4000,
      });
    } else {
      toast.add({
        severity: "error",
        summary: t("me.promotion.errorTitle"),
        detail: t("me.promotion.errorDetail"),
        life: 4000,
      });
    }
  }

  return { me, loading, promoting, fetchMe, requestPromotion };
}
