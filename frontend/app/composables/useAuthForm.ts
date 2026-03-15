import type { LoginRequest } from "~/types/auth";

export function useAuthForm() {
  const { t, te } = useI18n();

  const form = reactive<LoginRequest>({
    email: "",
    password: "",
  });
  const loading = ref(false);
  const errorMsg = ref<string | null>(null);
  const fieldErrors = ref<Record<string, string>>({});

  function resetErrors() {
    errorMsg.value = null;
    fieldErrors.value = {};
  }

  // Devuelve la traducción si existe, o la clave literal si no
  function tr(key: string) {
    return te(key) ? t(key) : key;
  }

  return { form, loading, errorMsg, fieldErrors, resetErrors, tr };
}
