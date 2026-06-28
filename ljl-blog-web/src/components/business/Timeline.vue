<script setup lang="ts">
import type { TimelineItem } from '@/types'
import { formatDate } from '@/utils'

defineOptions({ name: 'AppTimeline' })

interface Props {
  items: TimelineItem[]
}

defineProps<Props>()
</script>

<template>
  <div class="timeline">
    <div
      v-for="(item, index) in items"
      :key="`${item.date}-${index}`"
      class="timeline__item reveal"
      :class="`reveal-delay-${Math.min(index + 1, 6)}`"
    >
      <div class="timeline__dot" aria-hidden="true" />
      <div class="timeline__content">
        <time class="timeline__date">{{ formatDate(item.date) }}</time>
        <h4 class="timeline__title">{{ item.title }}</h4>
        <p class="timeline__desc">{{ item.description }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.timeline {
  position: relative;
  padding-left: $spacing-lg;

  &::before {
    content: '';
    position: absolute;
    left: 6px;
    top: 8px;
    bottom: 8px;
    width: 2px;
    background: repeating-linear-gradient(
      180deg,
      var(--pixel-border) 0,
      var(--pixel-border) 6px,
      transparent 6px,
      transparent 12px
    );
  }

  &__item {
    position: relative;
    padding-bottom: $spacing-lg;

    &:last-child {
      padding-bottom: 0;
    }
  }

  &__dot {
    position: absolute;
    left: -#{$spacing-lg - 2px};
    top: 6px;
    width: 12px;
    height: 12px;
    background: var(--pixel-accent-strong);
    border: 2px solid var(--color-bg);
    border-radius: $radius-sm;
    box-shadow: 0 0 0 2px var(--pixel-accent);
  }

  &__date {
    display: block;
    font-size: $font-size-xs;
    font-family: $font-family-pixel;
    color: var(--pixel-accent);
    margin-bottom: $spacing-xs;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    line-height: 1.6;
  }
}
</style>
