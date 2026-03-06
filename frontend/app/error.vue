<script setup lang="ts">
import { ROUTES } from "~/constants/routes";
import type { NuxtError } from "#app";
import type { ProblemDetail } from "~/types/api";

useHead({
  htmlAttrs: { class: "dark" },
});

const { t, te } = useI18n();
const props = defineProps<{
  error: NuxtError;
}>();

const pd = computed<ProblemDetail | null>(() => {
  const anyErr = props.error as any;
  return (anyErr?.data as ProblemDetail) ?? (anyErr?.cause?.data as ProblemDetail) ?? null;
});

const status = computed(() => (pd.value?.status as number | undefined) ?? props.error.status ?? 500);

// Los títulos siempre en inglés para consistencia porque el back los manda en inglés siempre
const heading = computed(() => {
  if (pd.value?.title) return pd.value.title;
  if (status.value === 400) return "Bad Request";
  if (status.value === 401) return "Unauthorized";
  if (status.value === 403) return "Forbidden";
  if (status.value === 404) return "Not Found";
  if (status.value === 409) return "Conflict";
  return "Unexpected Error";
});

const message = computed(() => {
  const code = pd.value?.code;

  if (typeof code === "string" && code.length) {
    return t(code);
  }

  if (pd.value?.detail) {
    return pd.value.detail;
  }

  const key = `error.${status.value}.message`;
  if (te(key)) return t(key);

  return t("error.unknown");
});

const goHome = () => clearError({ redirect: ROUTES.home });
</script>

<template>
  <!-- header a pelo porque la docu oficial recomienda no usar shells ni layouts, sino renderizar solo esta página en caso de error -->
  <div class="min-h-screen flex flex-column">
    <!-- Header -->
    <header class="surface-0 mt-2">
      <div class="mx-auto w-full max-w-7xl px-3 py-1 flex align-items-center justify-content-between gap-2">
        <NuxtLink :to="ROUTES.home" class="no-underline flex align-items-center">
          <img src="/banner.png" alt="Rockalendar" style="margin-top: -1.5rem; margin-bottom: -1.5rem; height: 5rem" />
        </NuxtLink>
      </div>
    </header>

    <!-- Body -->
    <main class="flex-1 surface-0 mb-5">
      <div class="mx-auto w-full max-w-7xl px-3 py-4">
        <header>
          <h1 id="error-name" class="flex flex-wrap justify-content-between mx-5">
            <span><i class="pi pi-exclamation-circle"></i></span><span>{{ heading }}</span>
            <span>{{ status }}</span>
          </h1>
        </header>
        <div class="flex justify-content-center">
          <Card class="w-full md:w-6 border-1 surface-border mt-6">
            <template #content>
              <div class="flex flex-column gap-4 text-center">
                <Message severity="error" :closable="false">
                  {{ message }}
                </Message>

                <div class="flex justify-content-center gap-2">
                  <Button :label="t('common.returnIndex')" icon="pi pi-home" @click="goHome" />
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </main>
  </div>
</template>
