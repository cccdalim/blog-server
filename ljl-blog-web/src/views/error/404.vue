<script setup lang="ts">
import MinimalLayout from '@/layouts/MinimalLayout.vue'
import AppButton from '@/components/common/AppButton.vue'
</script>

<template>
  <MinimalLayout>
    <div class="not-found">
      <div class="not-found__blocks" aria-hidden="true">
        <span class="not-found__block not-found__block--grass" />
        <span class="not-found__block not-found__block--stone" />
      </div>
      <h1 class="not-found__code">404</h1>
      <p class="not-found__text">这个方块不存在</p>
      <p class="not-found__hint">页面可能已被移除，或路径输入有误</p>
      <RouterLink to="/">
        <AppButton variant="primary" size="lg">返回首页</AppButton>
      </RouterLink>
    </div>
  </MinimalLayout>
</template>

<style scoped lang="scss">
.not-found {
  text-align: center;
  padding: $spacing-2xl;

  &__blocks {
    display: flex;
    justify-content: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-lg;
  }

  &__block {
    width: 32px;
    height: 32px;
    border-radius: $radius-sm;
    @include pixel-border;

    &--grass {
      background: var(--pixel-grass);
    }

    &--stone {
      background: var(--pixel-stone);
    }
  }

  &__code {
    font-size: 80px;
    font-weight: 900;
    font-family: $font-family-pixel;
    color: var(--pixel-accent-strong);
    line-height: 1;
    margin-bottom: $spacing-md;
  }

  &__text {
    font-size: $font-size-xl;
    color: var(--color-text-primary);
    margin-bottom: $spacing-sm;
  }

  &__hint {
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-xl;
  }
}
</style>
