<script setup lang="ts">
import AppCard from '@/components/common/AppCard.vue'
import type { Photo } from '@/types'
import { formatDate } from '@/utils'

interface Props {
  photo: Photo
}

defineProps<Props>()
</script>

<template>
  <div class="album-card">
    <AppCard padding="sm">
      <div class="album-card__image img-zoom">
        <img :src="photo.url" :alt="photo.title" loading="lazy" />
      </div>
      <div class="album-card__info">
        <h3 class="album-card__title">{{ photo.title }}</h3>
        <p class="album-card__location">
          {{ photo.city }}, {{ photo.country }}
        </p>
        <p class="album-card__date">{{ formatDate(photo.date) }}</p>
      </div>
    </AppCard>
  </div>
</template>

<style scoped lang="scss">
.album-card {
  &__image {
    border-radius: $radius-sm;
    overflow: hidden;
    margin-bottom: $spacing-sm;

    img {
      width: 100%;
      height: auto;
      display: block;
    }
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
  }

  &__date {
    font-size: $font-size-xs;
    color: var(--color-text-muted);
    margin-top: 2px;
  }
}
</style>
