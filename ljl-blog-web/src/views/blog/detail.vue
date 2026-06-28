<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppTag from '@/components/common/AppTag.vue'
import ReadingProgress from '@/components/common/ReadingProgress.vue'
import DocToc from '@/components/markdown/DocToc.vue'
import MarkdownViewer from '@/components/markdown/MarkdownViewer.vue'
import ContentLayout from '@/layouts/ContentLayout.vue'
import { useMarkdownRenderer } from '@/composables/useMarkdown'
import { useReadingProgress } from '@/composables/useReadingProgress'
import { useTocHighlight } from '@/composables/useTocHighlight'
import { useBlogStore } from '@/stores/blog'
import { formatDate } from '@/utils'

const route = useRoute()
const blogStore = useBlogStore()
const { currentArticle, articleNav, detailLoading } = storeToRefs(blogStore)

const articleRef = ref<HTMLElement | null>(null)
const mobileTocOpen = ref(false)
const loadError = ref(false)
const { extractToc } = useMarkdownRenderer()

function decodeRouteParam(value: string | string[] | undefined): string {
  const raw = Array.isArray(value) ? value[0] : value
  if (!raw) return ''
  try {
    return decodeURIComponent(raw)
  } catch {
    return raw
  }
}

const slug = computed(() => decodeRouteParam(route.params.slug))

const tocItems = computed(() => {
  if (!currentArticle.value?.content) return []
  return extractToc(currentArticle.value.content)
})

const { progress } = useReadingProgress(articleRef)
const { activeId, scrollToHeading } = useTocHighlight(tocItems)

async function loadArticle(articleSlug: string) {
  if (!articleSlug) return
  loadError.value = false
  await blogStore.loadDetail(articleSlug)
  if (slug.value !== articleSlug) return
  if (!currentArticle.value) {
    loadError.value = true
  }
}

function onTocClick(id: string) {
  scrollToHeading(id)
  mobileTocOpen.value = false
}

onBeforeRouteLeave((to) => {
  // 上下篇切换时复用同一组件，不清空数据
  if (to.name === 'BlogDetail') return
  blogStore.clearDetail()
})

watch(
  slug,
  (newSlug, oldSlug) => {
    if (!newSlug || newSlug === oldSlug) return
    loadArticle(newSlug)
    window.scrollTo({ top: 0 })
  },
  { immediate: true },
)
</script>

<template>
  <ReadingProgress :progress="progress" />

  <ContentLayout :show-mobile-sidebar="mobileTocOpen">
    <template #sidebar>
      <DocToc :items="tocItems" :active-id="activeId" @click="onTocClick" />
    </template>

    <template #toolbar>
      <button
        type="button"
        class="article-detail__toc-toggle"
        @click="mobileTocOpen = !mobileTocOpen"
      >
        {{ mobileTocOpen ? '✕ 关闭目录' : '☰ 目录' }}
      </button>
    </template>

    <div v-if="detailLoading" class="article-detail__loading">加载中...</div>

    <div v-else-if="loadError" class="article-detail__error">
      <p>文章不存在或加载失败</p>
      <RouterLink to="/blog" class="article-detail__back">← 返回 Blog 列表</RouterLink>
    </div>

    <article
      v-else-if="currentArticle"
      ref="articleRef"
      class="article-detail"
    >
      <header class="article-detail__header">
        <RouterLink to="/blog" class="article-detail__back">← 返回 Blog 列表</RouterLink>
        <h1 class="article-detail__title">{{ currentArticle.title }}</h1>
        <div class="article-detail__meta">
          <RouterLink
            :to="{ path: '/blog', query: { category: currentArticle.categorySlug } }"
            class="article-detail__category"
          >
            {{ currentArticle.category }}
          </RouterLink>
          <span class="article-detail__date">{{ formatDate(currentArticle.publishDate) }}</span>
          <span v-if="currentArticle.readTime" class="article-detail__read-time">
            {{ currentArticle.readTime }} 分钟阅读
          </span>
        </div>
        <div v-if="currentArticle.tags.length" class="article-detail__tags">
          <RouterLink
            v-for="tag in currentArticle.tags"
            :key="tag"
            :to="{ path: '/blog', query: { tag } }"
            class="article-detail__tag-link"
          >
            <AppTag :label="tag" variant="grass" />
          </RouterLink>
        </div>
      </header>

      <MarkdownViewer v-if="currentArticle.content" :content="currentArticle.content" />

      <nav v-if="articleNav.prev || articleNav.next" class="article-detail__nav">
        <RouterLink
          v-if="articleNav.prev"
          :to="{ name: 'BlogDetail', params: { slug: articleNav.prev.slug } }"
          class="article-detail__nav-link article-detail__nav-link--prev"
        >
          <span class="article-detail__nav-label">上一篇</span>
          <span class="article-detail__nav-title">{{ articleNav.prev.title }}</span>
        </RouterLink>
        <div v-else class="article-detail__nav-placeholder" />

        <RouterLink
          v-if="articleNav.next"
          :to="{ name: 'BlogDetail', params: { slug: articleNav.next.slug } }"
          class="article-detail__nav-link article-detail__nav-link--next"
        >
          <span class="article-detail__nav-label">下一篇</span>
          <span class="article-detail__nav-title">{{ articleNav.next.title }}</span>
        </RouterLink>
      </nav>
    </article>
  </ContentLayout>

  <div v-if="mobileTocOpen" class="article-detail__overlay" @click="mobileTocOpen = false" />
</template>

<style scoped lang="scss">
.article-detail {
  &__toc-toggle {
    display: none;
    @include pixel-button;
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
    border-radius: $radius-sm;
    margin-bottom: $spacing-md;

    @include mobile {
      display: inline-flex;
    }
  }

  &__loading,
  &__error {
    text-align: center;
    padding: $spacing-3xl;
    color: var(--color-text-muted);
  }

  &__header {
    margin-bottom: $spacing-xl;
    padding-bottom: $spacing-lg;
    border-bottom: 2px solid var(--color-border);
  }

  &__back {
    display: inline-block;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-md;

    &:hover {
      color: var(--pixel-accent);
    }
  }

  &__title {
    font-size: $font-size-2xl;
    font-weight: 800;
    color: var(--color-text-primary);
    line-height: 1.3;
    margin-bottom: $spacing-md;

    @include mobile {
      font-size: $font-size-xl;
    }
  }

  &__meta {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-md;
  }

  &__category {
    color: var(--pixel-accent);
    font-weight: 600;

    &:hover {
      text-decoration: underline;
    }
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__tag-link {
    display: inline-flex;
    color: inherit;

    &:hover {
      transform: translateY(-1px);
    }
  }

  &__nav {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $spacing-md;
    margin-top: $spacing-2xl;
    padding-top: $spacing-lg;
    border-top: 2px solid var(--color-border);

    @include mobile {
      grid-template-columns: 1fr;
    }
  }

  &__nav-link {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    padding: $spacing-md;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    transition:
      border-color var(--transition-fast),
      transform var(--transition-fast);

    &:hover {
      border-color: var(--pixel-accent);
      transform: translateY(-2px);
      color: inherit;
    }

    &--next {
      text-align: right;
    }
  }

  &__nav-label {
    font-size: $font-size-xs;
    color: var(--color-text-muted);
  }

  &__nav-title {
    font-size: $font-size-sm;
    font-weight: 600;
    color: var(--color-text-primary);
    @include text-ellipsis(2);
  }

  &__nav-placeholder {
    @include mobile {
      display: none;
    }
  }

  &__overlay {
    position: fixed;
    inset: 0;
    top: var(--navbar-height);
    background: rgba(0, 0, 0, 0.5);
    z-index: calc(#{$z-sidebar} - 1);

    @include tablet-up {
      display: none;
    }
  }
}
</style>
