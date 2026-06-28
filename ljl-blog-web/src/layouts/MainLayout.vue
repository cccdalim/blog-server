<script setup lang="ts">
import { provide, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import BackTop from '@/components/common/BackTop.vue'
import { REVEAL_REFRESH_KEY, useLayoutScrollReveal } from '@/composables/useScrollReveal'

const route = useRoute()
const contentRef = ref<HTMLElement | null>(null)
const { refreshReveal } = useLayoutScrollReveal(contentRef)

provide(REVEAL_REFRESH_KEY, refreshReveal)

watch(
  () => route.fullPath,
  () => window.scrollTo({ top: 0 }),
)
</script>

<template>
  <div class="main-layout">
    <AppNavbar />
    <main ref="contentRef" class="main-layout__content">
      <RouterView v-slot="{ Component }">
        <component :is="Component" :key="String(route.name ?? route.path)" />
      </RouterView>
    </main>
    <AppFooter />
    <BackTop />
  </div>
</template>

<style scoped lang="scss">
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;

  &__content {
    flex: 1;
    padding-top: var(--navbar-height);
  }
}
</style>
