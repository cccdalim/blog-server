<script setup lang="ts">
import { useWindowSize } from '@vueuse/core'
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ThemeSwitch from '@/components/common/ThemeSwitch.vue'
import MinecraftVines from '@/components/layout/MinecraftVines.vue'
import { NAV_ITEMS, SITE_NAME, type NavItem } from '@/constants'

const route = useRoute()
const router = useRouter()
const { width } = useWindowSize()
const menuOpen = ref(false)

const isMobile = computed(() => width.value < 768)

function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function closeMenu() {
  menuOpen.value = false
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

/** 回到首页 — 已在首页则滚到顶部 */
function goHome() {
  closeMenu()
  if (route.path === '/') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  router.push('/')
}

function onNavClick(item: NavItem, event: MouseEvent) {
  closeMenu()
  if (item.path === '/') {
    event.preventDefault()
    goHome()
    return
  }
  // 从详情页（/blog/:slug、/docs/:slug 等）点击对应导航，强制回到列表根路径
  if (item.path !== '/' && route.path.startsWith(`${item.path}/`)) {
    event.preventDefault()
    router.push(item.path)
  }
}

watch(
  () => route.fullPath,
  () => closeMenu(),
)
</script>

<template>
  <header class="navbar">
    <MinecraftVines />
    <div class="navbar__inner container">
      <RouterLink to="/" class="navbar__brand" @click.prevent="goHome">
        <span class="navbar__logo">⬛</span>
        <span class="navbar__title">{{ SITE_NAME }}</span>
      </RouterLink>

      <!-- Desktop nav -->
      <nav v-if="!isMobile" class="navbar__nav">
        <RouterLink
          v-for="item in NAV_ITEMS"
          :key="item.path"
          :to="item.path"
          class="navbar__link"
          :class="{ 'navbar__link--active': isActive(item.path) }"
          @click="onNavClick(item, $event)"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="navbar__actions">
        <ThemeSwitch />
        <button
          v-if="isMobile"
          class="navbar__hamburger"
          :aria-expanded="menuOpen"
          aria-label="打开菜单"
          @click="toggleMenu"
        >
          <span :class="{ 'is-open': menuOpen }" />
        </button>
      </div>
    </div>

    <!-- Mobile drawer -->
    <Transition name="slide-up">
      <nav v-if="isMobile && menuOpen" class="navbar__mobile">
        <RouterLink
          v-for="item in NAV_ITEMS"
          :key="item.path"
          :to="item.path"
          class="navbar__mobile-link"
          :class="{ 'navbar__mobile-link--active': isActive(item.path) }"
          @click="onNavClick(item, $event)"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </Transition>
  </header>
</template>

<style scoped lang="scss">
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-navbar;
  height: var(--navbar-height);
  background: rgba($color-bg-dark, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 2px solid var(--color-border);
  overflow: visible;

  [data-theme='light'] & {
    background: rgba($color-bg-light, 0.92);
  }

  &__inner {
    position: relative;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    color: var(--color-text-primary);
    font-weight: 700;
    font-size: $font-size-base;

    &:hover {
      color: var(--pixel-accent);
    }
  }

  &__logo {
    font-size: $font-size-lg;
    color: var(--pixel-logo);
  }

  &__title {
    @include mobile {
      font-size: $font-size-sm;
      max-width: 140px;
      @include text-ellipsis(1);
    }
  }

  &__nav {
    display: flex;
    gap: $spacing-xs;
  }

  &__link {
    padding: $spacing-xs $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    border-radius: $radius-sm;
    transition:
      color var(--transition-fast),
      background var(--transition-fast);

    &:hover {
      color: var(--color-text-primary);
      background: var(--color-bg-elevated);
    }

    &--active {
      color: var(--pixel-accent);
      background: var(--pixel-grass-bg-hover);
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__hamburger {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;

    span,
    span::before,
    span::after {
      display: block;
      width: 20px;
      height: 2px;
      background: var(--color-text-primary);
      transition: transform var(--transition-base);
      position: relative;
    }

    span::before,
    span::after {
      content: '';
      position: absolute;
      left: 0;
    }

    span::before {
      top: -6px;
    }

    span::after {
      top: 6px;
    }

    span.is-open {
      background: transparent;

      &::before {
        top: 0;
        transform: rotate(45deg);
      }

      &::after {
        top: 0;
        transform: rotate(-45deg);
      }
    }
  }

  &__mobile {
    position: absolute;
    top: var(--navbar-height);
    left: 0;
    right: 0;
    background: var(--color-bg-card);
    border-bottom: 2px solid var(--color-border);
    padding: $spacing-md;
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
  }

  &__mobile-link {
    padding: $spacing-md;
    font-size: $font-size-base;
    color: var(--color-text-secondary);
    border-radius: $radius-sm;
    @include pixel-border(transparent);

    &--active {
      color: var(--pixel-accent);
      border-color: var(--pixel-accent-strong);
      background: var(--pixel-grass-bg-subtle);
    }
  }
}
</style>
