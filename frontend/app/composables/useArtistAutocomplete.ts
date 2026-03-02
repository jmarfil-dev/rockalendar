type Artist = { id: string; name: string };

export const useArtistAutocomplete = () => {
  const suggestions = ref<Artist[]>([]);
  const loading = ref(false);

  const cache = useState<Record<string, Artist[]>>("artistAcCache", () => ({}));
  let t: ReturnType<typeof setTimeout> | null = null;

  const search = (q: string) => {
    const query = q.trim();
    if (t) clearTimeout(t);

    if (query.length < 2 || query.length > 50) {
      suggestions.value = [];
      loading.value = false;
      return;
    }

    t = setTimeout(async () => {
      if (cache.value[query]) {
        suggestions.value = cache.value[query];
        return;
      }

      loading.value = true;
      try {
        const { data, error } = await useFetch<Artist[]>("/api/artists", {
          query: { query }, // ajusta si el back usa otro nombre
        });
        if (error.value) throw error.value;
        const res = data.value ?? [];
        cache.value[query] = res;
        suggestions.value = res;
      } finally {
        loading.value = false;
      }
    }, 250);
  };

  return { suggestions, loading, search };
};
