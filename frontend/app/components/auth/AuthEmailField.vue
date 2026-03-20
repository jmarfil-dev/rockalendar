<script setup lang="ts">
defineProps<{
  modelValue: string;
  fieldError?: string;
  required?: boolean;
  showRequired?: boolean;
}>();
defineEmits<{ "update:modelValue": [value: string] }>();

const { t, te } = useI18n();
function tr(key: string) {
  return te(key) ? t(key) : key;
}
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
      :modelValue="modelValue"
      @update:modelValue="$emit('update:modelValue', $event ?? '')"
      type="email"
      inputmode="email"
      autocomplete="email"
      :required="required"
      :invalid="!!fieldError"
      aria-describedby="email-error" />
    <Message id="email-error" v-show="fieldError" severity="error" variant="simple" size="small">
      {{ tr(fieldError!) }}
    </Message>
  </div>
</template>
