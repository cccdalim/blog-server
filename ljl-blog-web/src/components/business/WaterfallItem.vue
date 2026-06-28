<script setup lang="ts">
import AppCard from '@/components/common/AppCard.vue'
import type { Photo } from '@/types'
import { formatDate } from '@/utils'

interface Props {
  photo: Photo
  previewList: string[]
  index: number
}

defineProps<Props>()
</script>

<template>
  <div class="waterfall-item">
    <AppCard padding="sm" :hoverable="true">
      <el-image
        :src="photo.url"
        :alt="photo.title"
        :preview-src-list="previewList"
        :initial-index="index"
        fit="cover"
        class="waterfall-item__image"
        loading="lazy"
      />
      <div class="waterfall-item__info">
        <h3 class="waterfall-item__title">{{ photo.title }}</h3>
        <p class="waterfall-item__location">{{ photo.city }}, {{ photo.country }}</p>
        <div class="waterfall-item__meta">
          <span class="waterfall-item__category">{{ photo.category }}</span>
          <span class="waterfall-item__date">{{ formatDate(photo.date) }}</span>
        </div>
      </div>
    </AppCard>
  </div>
</template>

<style scoped lang="scss">
.waterfall-item {
  break-inside: avoid;
  margin-bottom: $spacing-md;

  &__image {
    width: 100%;
    border-radius: $radius-sm;
    overflow: hidden;
    cursor: zoom-in;
    display: block;

    :deep(.el-image__inner) {
      width: 100%;
      height: auto;
      display: block;
      transition: transform var(--transition-slow);
    }

    &:hover :deep(.el-image__inner) {
      transform: scale(1.05);
    }
  }

  &__info {
    padding-top: $spacing-sm;
  }

  &__title {
    font-size: $font-size-sm;
    font-weight: 600;
    color: var(--color-text-primary);
    margin-bottom: 2px;
  }

  &__location {
    font-size: $font-size-xs;
    color: var(--pixel-accent);
    margin-bottom: $spacing-xs;
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: $font-size-xs;
  }

  &__category {
    padding: 2px $spacing-xs;
    background: var(--pixel-grass-bg-subtle);
    color: var(--pixel-accent);
    border-radius: $radius-sm;
  }

  &__date {
    color: var(--color-text-muted);
  }
}
</style>
