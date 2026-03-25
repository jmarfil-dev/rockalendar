// @ts-check
import withNuxt from './.nuxt/eslint.config.mjs'
import sonarjs from 'eslint-plugin-sonarjs'

export default withNuxt(
  sonarjs.configs.recommended,
  {
    rules: {
      // Vue 3 soporta múltiples elementos raíz (fragments)
      'vue/no-multiple-template-root': 'off',
      // Regla no encontrada en el entorno actual
      'n/no-unsupported-features/node-builtins': 'off',
    },
  },
  {
    // forbidden.vue tiene template vacío intencionalmente: showError() redirige antes de renderizar
    files: ['**/pages/error/forbidden.vue'],
    rules: { 'vue/valid-template-root': 'off' },
  }
)
