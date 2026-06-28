<script setup lang="ts">
import { onMounted } from 'vue'
import AlbumCard from '@/components/business/AlbumCard.vue'
import { HOME_LIMITS } from '@/constants'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useAlbumStore } from '@/stores/album'
import { storeToRefs } from 'pinia'

const albumStore = useAlbumStore()
const { recentPhotos, loading } = storeToRefs(albumStore)

useRevealAfterLoad(loading)

onMounted(() => {
  albumStore.loadRecent(HOME_LIMITS.photos)
})
</script>

<template>
  <section class="section reveal">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">旅行照片</h2>
        <RouterLink to="/album" class="section-link">查看全部 →</RouterLink>
      </div>

      <div v-if="loading" class="section-loading">加载中...</div>

      <div v-else class="album-grid">
        <AlbumCard
          v-for="(photo, index) in recentPhotos"
          :key="photo.id"
          :photo="photo"
          :class="['reveal', `reveal-delay-${index + 1}`]"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.album-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;

  @include tablet {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-sm;
  }
}

.section-loading {
  text-align: center;
  color: var(--color-text-muted);
  padding: $spacing-2xl;
}
</style>
