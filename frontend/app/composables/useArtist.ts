import { ROUTES, ROUTE_PATH } from "~/constants/routes";
import type { Artist } from "~/types/artist";

export const useArtistAutocomplete = () => {
  const suggestions = ref<Artist[]>([]);
  const loading = ref(false);

  // cache de autocomplete (global, pero creado en contexto Nuxt)
  const cache = useState<Record<string, Artist[]>>("artistAcCache", () => ({}));

  let t: ReturnType<typeof setTimeout> | null = null;

  onUnmounted(() => {
    if (t) clearTimeout(t);
  });

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
        const res = await $fetch<Artist[]>(ROUTES.apiArtists, {
          query: { query },
        });

        const list = res ?? [];
        cache.value[query] = list;
        suggestions.value = list;
      } catch (err) {
        if (import.meta.dev) console.error("Error loading artists", err);
        suggestions.value = [];
      } finally {
        loading.value = false;
      }
    }, 250);
  };

  return { suggestions, loading, search };
};

export const clearArtistAutocompleteCache = () => {
  const cache = useState<Record<string, Artist[]>>("artistAcCache", () => ({}));
  cache.value = {};
};

export async function fetchArtistById(id: string): Promise<Artist | null> {
  // cache por id (global, pero creado en contexto Nuxt)
  const artistCache = useState<Record<string, Artist>>("artistCache", () => ({}));

  if (artistCache.value[id]) return artistCache.value[id];

  try {
    const a = await $fetch<Artist>(ROUTE_PATH.apiArtistDetail(id));
    artistCache.value[id] = a;
    return a;
  } catch {
    return null;
  }
}
