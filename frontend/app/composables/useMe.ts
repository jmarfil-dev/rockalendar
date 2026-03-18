import { ROUTES } from "~/constants/routes";
import type { MeDto } from "~/types/user";

export function useMe() {
  const auth = useAuth();
  const { t } = useI18n();
  const toast = useToast();

  const me = ref<MeDto | null>(null);
  const loading = ref(false);
  const promoting = ref(false);
  let logoutTimer: ReturnType<typeof setTimeout> | null = null;

  onUnmounted(() => {
    if (logoutTimer !== null) clearTimeout(logoutTimer);
  });

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
      toast.add({
        severity: "success",
        summary: t("me.promotion.successTitle"),
        detail: t("me.promotion.successDetail"),
        life: 6000,
      });
      // El JWT actual no tiene el rol actualizado: hay que renovar la sesión
      logoutTimer = setTimeout(() => auth.logout(), 3000);
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
