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
      // El backend devuelve { ineCode, name }; lo normalizamos a { id, name }
      const data = await $fetch<Array<{ ineCode: number; name: string }>>(ROUTES.apiProvincesCombo);
      provinces.value = (data ?? []).map((p) => ({ id: p.ineCode, name: p.name }));
      loaded.value = true;
    } finally {
      loading.value = false;
    }
  };

  const options = computed(() => provinces.value.map((p) => ({ label: p.name, value: p.id })));

  return { provinces, options, load, loading };
};
