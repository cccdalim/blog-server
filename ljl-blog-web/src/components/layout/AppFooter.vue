<script setup lang="ts">
import { NAV_ITEMS, SITE_DESCRIPTION, SITE_NAME } from '@/constants'
</script>

<template>
  <footer class="footer">
    <div class="footer__inner container">
      <div class="footer__brand">
        <p class="footer__name">{{ SITE_NAME }}</p>
        <p class="footer__desc">{{ SITE_DESCRIPTION }}</p>
      </div>

      <nav class="footer__nav">
        <RouterLink
          v-for="item in NAV_ITEMS"
          :key="item.path"
          :to="item.path"
          class="footer__link"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="footer__bottom">
        <p class="footer__copy">
          © {{ new Date().getFullYear() }} LJL. Built with Vue 3 + Vite.
        </p>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
.footer {
  margin-top: auto;
  border-top: 2px solid var(--color-border);
  background: var(--color-bg-card);
  padding-block: $spacing-2xl $spacing-lg;

  &__inner {
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;
  }

  &__name {
    font-size: $font-size-lg;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-size-sm;
    color: var(--color-text-muted);
  }

  &__nav {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm $spacing-lg;
  }

  &__link {
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    transition: color var(--transition-fast);

    &:hover {
      color: var(--pixel-accent);
    }
  }

  &__bottom {
    padding-top: $spacing-lg;
    border-top: 1px solid var(--color-border);
  }

  &__copy {
    font-size: $font-size-xs;
    color: var(--color-text-muted);
  }
}
</style>
