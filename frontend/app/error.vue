<script setup lang="ts">
import type { NuxtError } from "#app";

useHead({
  htmlAttrs: { class: "dark" },
});

const { t } = useI18n();
const props = defineProps<{
  error: NuxtError;
}>();

type ProblemDetail = {
  status?: number;
  title?: string;
  detail?: string;
  instance?: string;
  type?: string;
  timestamp?: string;
  [key: string]: unknown;
};

const pd = computed<ProblemDetail | null>(() => {
  const anyErr = props.error as any;
  return (anyErr?.data as ProblemDetail) ?? null;
});

const type = computed(() => pd.value?.type ?? "");
const status = computed(() => (pd.value?.status as number | undefined) ?? props.error.status ?? 500);

const heading = computed(() => {
  if (pd.value?.title) return pd.value.title;

  switch (type.value) {
    case "urn:rockalendar:error:validation": // 400
      return "Invalid data";
    case "urn:rockalendar:error:bad-request": // 400
      return "Bad Request";
    case "urn:rockalendar:error:unauthorized": // 401
      return "Unauthorized";
    case "urn:rockalendar:error:forbidden": // 403
      return "Forbidden";
    case "urn:rockalendar:error:not-found": // 404
      return "Not Found";
    case "urn:rockalendar:error:conflict": // 409
      return "Conflict";
    case "urn:rockalendar:error:moderation-state": // 409
      return "Conflict on Moderation action";
    case "urn:rockalendar:error:event-state": // 409
      return "Conflict on Event action";

    default:
      return "ERROR";
  }
});

const message = computed(() => {
  if (pd.value?.detail) {
    return pd.value.detail;
  } else {
    return "Unknown error";
  }
});

const goHome = () => clearError({ redirect: "/" });
</script>

<template>
  <AppShell>
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
  </AppShell>
</template>
