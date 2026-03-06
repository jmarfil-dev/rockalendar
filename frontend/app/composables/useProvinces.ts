import type { Province } from "~/types/geo";
import { ROUTES } from "~/constants/routes";

export const useProvinces = () => {
  const provinces = useState<Province[]>("provinces", () => []);
  const loaded = useState<boolean>("provincesLoaded", () => false);
  const loading = useState<boolean>("provincesLoading", () => false);

  const load = async () => {
    if (loaded.value || loading.value) return;

    loading.value = true;
    try {
      const data = await $fetch<Province[]>(ROUTES.apiProvincesCombo);
      provinces.value = data ?? [];
      loaded.value = true;
    } finally {
      loading.value = false;
    }
  };

  const options = computed(() => provinces.value.map((p) => ({ label: p.name, value: p.id })));

  return { provinces, options, load, loading };
};
