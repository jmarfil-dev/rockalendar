<script setup lang="ts">
defineProps<{
  modelValue: string;
  fieldError?: string;
  required?: boolean;
  showRequired?: boolean;
}>();
defineEmits<{ "update:modelValue": [value: string] }>();

const { t } = useI18n();
</script>

<template>
  <div class="flex flex-column gap-2">
    <label for="email" class="text-sm text-color-secondary">
      {{ t("user.email") }}
      <template v-if="showRequired">
        <span class="text-red-500" aria-hidden="true">*</span><span class="sr-only">{{ t("common.required") }}</span>
      </template>
    </label>
    <InputText
      id="email"
      :model-value="modelValue"
      type="email"
      inputmode="email"
      autocomplete="email"
      :required="required"
      :invalid="!!fieldError"
      aria-describedby="email-error"
      @update:model-value="$emit('update:modelValue', $event ?? '')" />
    <Message v-show="fieldError" id="email-error" severity="error" variant="simple" size="small">
      {{ tr(fieldError!) }}
    </Message>
  </div>
</template>
