import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ThemeMode = 'dark' | 'light'

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>('dark')

  function applyTheme() {
    document.documentElement.setAttribute('data-theme', mode.value)
  }

  function toggleTheme() {
    mode.value = mode.value === 'dark' ? 'light' : 'dark'
    applyTheme()
  }

  function initTheme() {
    applyTheme()
  }

  return { mode, toggleTheme, initTheme }
})
