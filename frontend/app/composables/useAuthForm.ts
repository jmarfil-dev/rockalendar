import type { LoginRequest } from "~/types/auth";

export function useAuthForm() {
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

  return { form, loading, errorMsg, fieldErrors, resetErrors };
}
