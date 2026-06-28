<script setup lang="ts">
import { onMounted } from 'vue'
import ArticleCard from '@/components/business/ArticleCard.vue'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useBlogStore } from '@/stores/blog'
import { storeToRefs } from 'pinia'
import { HOME_LIMITS } from '@/constants'

const blogStore = useBlogStore()
const { latestArticles, loading } = storeToRefs(blogStore)

useRevealAfterLoad(loading)

onMounted(() => {
  blogStore.loadLatest(HOME_LIMITS.blog)
})
</script>

<template>
  <section class="section reveal">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">最新博客</h2>
        <RouterLink to="/blog" class="section-link">查看全部 →</RouterLink>
      </div>

      <div v-if="loading" class="section-loading">加载中...</div>

      <div v-else class="section-grid">
        <ArticleCard
          v-for="(article, index) in latestArticles"
          :key="article.id"
          :article="article"
          link-prefix="/blog"
          :class="`reveal reveal-delay-${index + 1}`"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.section-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;

  @include tablet {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
  }
}

.section-loading {
  text-align: center;
  color: var(--color-text-muted);
  padding: $spacing-2xl;
}
</style>
