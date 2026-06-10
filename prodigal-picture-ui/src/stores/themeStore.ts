import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { theme } from 'ant-design-vue'

const { darkAlgorithm } = theme

const THEME_KEY = 'theme'

export const useThemeStore = defineStore('theme', () => {
  const darkMode = ref(false)

  const algorithm = computed(() => darkMode.value ? darkAlgorithm : undefined)

  function init() {
    const stored = localStorage.getItem(THEME_KEY)
    darkMode.value = stored === 'dark'
    if (darkMode.value) {
      document.documentElement.classList.add('dark')
    }
  }

  function toggle() {
    darkMode.value = !darkMode.value
    localStorage.setItem(THEME_KEY, darkMode.value ? 'dark' : 'light')

    document.documentElement.classList.add('transitioning')
    document.documentElement.classList.toggle('dark', darkMode.value)
    setTimeout(() => {
      document.documentElement.classList.remove('transitioning')
    }, 350)
  }

  return { darkMode, algorithm, init, toggle }
})
