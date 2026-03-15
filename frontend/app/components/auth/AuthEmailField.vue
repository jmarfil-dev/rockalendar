<script setup lang="ts">
defineProps<{
  modelValue: string;
  fieldError?: string;
  required?: boolean;
}>();
defineEmits<{ "update:modelValue": [value: string] }>();

const { t, te } = useI18n();
function tr(key: string) {
  return te(key) ? t(key) : key;
}
</script>

<template>
  <div class="flex flex-column gap-2">
    <label for="email" class="text-sm text-color-secondary">{{ t("user.email") }}</label>
    <InputText
      id="email"
      :modelValue="modelValue"
      @update:modelValue="$emit('update:modelValue', $event)"
      type="email"
      inputmode="email"
      autocomplete="email"
      :required="required"
      :invalid="!!fieldError" />
    <Message v-show="fieldError" severity="error" variant="simple" size="small">
      {{ tr("fieldErrors.email") }}
    </Message>
  </div>
</template>
