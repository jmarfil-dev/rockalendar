type Province = { id: string; name: string }; // ajusta al DTO real

export const useProvinces = () => {
  const provinces = useState<Province[]>("provinces", () => []);
  const loaded = useState<boolean>("provincesLoaded", () => false);

  const load = async () => {
    if (loaded.value) return;
    const { data, error } = await useFetch<Province[]>("/api/provinces/combo");
    if (error.value) throw error.value;
    provinces.value = data.value ?? [];
    loaded.value = true;
  };

  const options = computed(() => provinces.value.map((p) => ({ label: p.name, value: p.id })));

  return { provinces, options, load };
};
