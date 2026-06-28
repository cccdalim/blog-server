<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const { mode } = storeToRefs(themeStore)
</script>

<template>
  <button
    class="theme-switch"
    :aria-label="mode === 'dark' ? '切换亮色模式' : '切换暗色模式'"
    @click="themeStore.toggleTheme()"
  >
    <span v-if="mode === 'dark'" class="theme-switch__icon">☀</span>
    <span v-else class="theme-switch__icon">☾</span>
  </button>
</template>

<style scoped lang="scss">
.theme-switch {
  @include pixel-border(var(--pixel-border));
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--color-bg-card);
  border-radius: $radius-sm;
  transition:
    transform var(--transition-fast),
    background var(--transition-fast);

  &:hover {
    transform: translateY(-1px);
    background: var(--color-bg-elevated);
  }

  &__icon {
    font-size: $font-size-base;
    line-height: 1;
  }
}
</style>
