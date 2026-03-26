<script setup lang="ts">
import { ROUTES } from "~/constants/routes";

definePageMeta({ layout: "public" });

const { t } = useI18n();
useHead({ title: () => t("page.contact") });
useSeoMeta({
  description: "Get in touch with the Rockalendar team.",
  ogDescription: "Get in touch with the Rockalendar team.",
  ogType: "website",
});

const form = reactive({ name: "", email: "", message: "" });
const submitting = ref(false);
const sent = ref(false);
const errorMsg = ref<string | null>(null);

async function onSubmit() {
  submitting.value = true;
  errorMsg.value = null;
  try {
    await $fetch(ROUTES.apiContact, {
      method: "POST",
      body: { name: form.name, email: form.email, message: form.message },
    });
    sent.value = true;
  } catch {
    errorMsg.value = t("common.errorGeneric");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="flex flex-column gap-4">
    <div class="flex align-items-center gap-3">
      <button class="p-0 border-none bg-transparent cursor-pointer text-color-secondary" :aria-label="t('common.back')" @click="$router.back()">
        <i class="pi pi-arrow-left" aria-hidden="true" />
      </button>
      <h1 class="text-2xl font-bold m-0">{{ t("contact.title") }}</h1>
    </div>

    <Card class="border-1 surface-border">
      <template #content>
        <Message v-if="sent" severity="success" :closable="false">
          {{ t("contact.successMsg") }}
        </Message>

        <template v-else>
          <Message v-if="errorMsg" severity="error" :closable="false" class="mb-3">{{ errorMsg }}</Message>

          <form class="flex flex-column gap-3" @submit.prevent="onSubmit">
            <div class="flex flex-column gap-2">
              <label for="contact-name" class="text-sm text-color-secondary">{{ t("contact.name") }}</label>
              <InputText
                id="contact-name"
                v-model="form.name"
                :placeholder="t('contact.namePlaceholder')"
                autocomplete="name" />
            </div>

            <div class="flex flex-column gap-2">
              <label for="contact-email" class="text-sm text-color-secondary">
                {{ t("user.email") }}
                <span class="text-red-500" aria-hidden="true">*</span>
                <span class="sr-only">{{ t("common.required") }}</span>
              </label>
              <InputText
                id="contact-email"
                v-model="form.email"
                type="email"
                autocomplete="email"
                required />
            </div>

            <div class="flex flex-column gap-2">
              <label for="contact-message" class="text-sm text-color-secondary">
                {{ t("contact.message") }}
                <span class="text-red-500" aria-hidden="true">*</span>
                <span class="sr-only">{{ t("common.required") }}</span>
              </label>
              <Textarea
                id="contact-message"
                v-model="form.message"
                :placeholder="t('contact.messagePlaceholder')"
                rows="5"
                required />
            </div>

            <Button type="submit" :label="t('contact.submit')" icon="pi pi-send" :loading="submitting" />
          </form>

          <p class="text-sm text-color-secondary mt-3 mb-0">{{ t("contact.note") }}</p>
        </template>
      </template>
    </Card>
  </div>
</template>
