<script setup lang="ts">
import AppCard from '@/components/common/AppCard.vue'
import AppTag from '@/components/common/AppTag.vue'
import type { Article } from '@/types'
import { formatDate } from '@/utils'

interface Props {
  article: Article
  linkPrefix?: string
}

withDefaults(defineProps<Props>(), {
  linkPrefix: '/blog',
})
</script>

<template>
  <RouterLink
    :to="{
      name: linkPrefix === '/docs' ? 'DocDetail' : 'BlogDetail',
      params: { slug: article.slug },
    }"
    class="article-card"
  >
    <AppCard padding="sm">
      <div class="article-card__cover img-zoom">
        <img
          v-if="article.cover"
          :src="article.cover"
          :alt="article.title"
          loading="lazy"
        />
      </div>
      <div class="article-card__body">
        <div class="article-card__meta">
          <span class="article-card__category">{{ article.category }}</span>
          <span class="article-card__date">{{ formatDate(article.publishDate) }}</span>
        </div>
        <h3 class="article-card__title">{{ article.title }}</h3>
        <p class="article-card__summary">{{ article.summary }}</p>
        <div v-if="article.tags.length" class="article-card__tags">
          <AppTag
            v-for="tag in article.tags.slice(0, 3)"
            :key="tag"
            :label="tag"
            variant="outline"
          />
        </div>
      </div>
    </AppCard>
  </RouterLink>
</template>

<style scoped lang="scss">
.article-card {
  display: block;
  color: inherit;
  height: 100%;

  &:hover {
    color: inherit;
  }

  &__cover {
    border-radius: $radius-sm;
    overflow: hidden;
    aspect-ratio: 16 / 9;
    margin-bottom: $spacing-md;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-sm;
    font-size: $font-size-xs;
  }

  &__category {
    color: var(--pixel-accent);
    font-weight: 600;
  }

  &__date {
    color: var(--color-text-muted);
  }

  &__title {
    font-size: $font-size-base;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: $spacing-sm;
    @include text-ellipsis(2);
  }

  &__summary {
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    margin-bottom: $spacing-md;
    @include text-ellipsis(2);
    line-height: 1.6;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }
}
</style>
