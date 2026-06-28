import { useLocalStorage } from '@vueuse/core'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'ljl-falling-leaves'

export const useEffectsStore = defineStore('effects', () => {
  const fallingLeavesEnabled = useLocalStorage<boolean>(STORAGE_KEY, true)

  function toggleFallingLeaves() {
    fallingLeavesEnabled.value = !fallingLeavesEnabled.value
  }

  function setFallingLeaves(enabled: boolean) {
    fallingLeavesEnabled.value = enabled
  }

  return { fallingLeavesEnabled, toggleFallingLeaves, setFallingLeaves }
})
