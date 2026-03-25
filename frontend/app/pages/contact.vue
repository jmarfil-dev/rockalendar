<script setup lang="ts">
definePageMeta({ layout: "public" });

const { t } = useI18n();
useHead({ title: () => t("page.contact") });

const form = reactive({ name: "", email: "", message: "" });

function onSubmit() {
  // eslint-disable-next-line sonarjs/todo-tag
  // TODO: conectar con backend cuando esté disponible
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

          <Button type="submit" :label="t('contact.submit')" icon="pi pi-send" />
        </form>

        <p class="text-sm text-color-secondary mt-3 mb-0">{{ t("contact.note") }}</p>
      </template>
    </Card>
  </div>
</template>
