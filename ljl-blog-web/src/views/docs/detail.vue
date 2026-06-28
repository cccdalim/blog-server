<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppTag from '@/components/common/AppTag.vue'
import ReadingProgress from '@/components/common/ReadingProgress.vue'
import DocToc from '@/components/markdown/DocToc.vue'
import MarkdownViewer from '@/components/markdown/MarkdownViewer.vue'
import ContentLayout from '@/layouts/ContentLayout.vue'
import { useMarkdownRenderer } from '@/composables/useMarkdown'
import { useReadingProgress } from '@/composables/useReadingProgress'
import { useTocHighlight } from '@/composables/useTocHighlight'
import { useDocsStore } from '@/stores/docs'
import { formatDate } from '@/utils'

const route = useRoute()
const router = useRouter()
const docsStore = useDocsStore()
const { currentDoc, docNav, detailLoading } = storeToRefs(docsStore)

const articleRef = ref<HTMLElement | null>(null)
const mobileTocOpen = ref(false)
const loadError = ref(false)
const { extractToc } = useMarkdownRenderer()

const slug = computed(() => route.params.slug as string)

const tocItems = computed(() => {
  if (!currentDoc.value?.content) return []
  return extractToc(currentDoc.value.content)
})

const { progress } = useReadingProgress(articleRef)
const { activeId, scrollToHeading } = useTocHighlight(tocItems)

async function loadDoc(docSlug: string) {
  if (!docSlug) return
  loadError.value = false
  await docsStore.loadDetail(docSlug)
  if (!currentDoc.value) {
    loadError.value = true
    router.replace({ name: 'NotFound' })
  }
}

function onTocClick(id: string) {
  scrollToHeading(id)
  mobileTocOpen.value = false
}

onBeforeRouteUpdate((to) => {
  const nextSlug = to.params.slug as string
  loadDoc(nextSlug)
  window.scrollTo({ top: 0 })
})

onBeforeRouteLeave(() => {
  docsStore.clearDetail()
})

watch(
  slug,
  (newSlug) => {
    loadDoc(newSlug)
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
      <button type="button" class="doc-detail__toc-toggle" @click="mobileTocOpen = !mobileTocOpen">
        {{ mobileTocOpen ? '✕ 关闭目录' : '☰ 目录' }}
      </button>
    </template>

    <div v-if="detailLoading" class="doc-detail__loading">加载中...</div>

    <article v-else-if="currentDoc && !loadError" ref="articleRef" class="doc-detail">
      <header class="doc-detail__header">
        <RouterLink to="/docs" class="doc-detail__back">← 返回文档列表</RouterLink>
        <h1 class="doc-detail__title">{{ currentDoc.title }}</h1>
        <div class="doc-detail__meta">
          <span class="doc-detail__category">{{ currentDoc.category }}</span>
          <span class="doc-detail__date">{{ formatDate(currentDoc.publishDate) }}</span>
          <span v-if="currentDoc.readTime" class="doc-detail__read-time">
            {{ currentDoc.readTime }} 分钟阅读
          </span>
        </div>
        <div v-if="currentDoc.tags.length" class="doc-detail__tags">
          <AppTag v-for="tag in currentDoc.tags" :key="tag" :label="tag" variant="grass" />
        </div>
      </header>

      <MarkdownViewer v-if="currentDoc.content" :content="currentDoc.content" />

      <nav v-if="docNav.prev || docNav.next" class="doc-detail__nav">
        <RouterLink
          v-if="docNav.prev"
          :to="`/docs/${docNav.prev.slug}`"
          class="doc-detail__nav-link doc-detail__nav-link--prev"
        >
          <span class="doc-detail__nav-label">上一篇</span>
          <span class="doc-detail__nav-title">{{ docNav.prev.title }}</span>
        </RouterLink>
        <div v-else class="doc-detail__nav-placeholder" />

        <RouterLink
          v-if="docNav.next"
          :to="`/docs/${docNav.next.slug}`"
          class="doc-detail__nav-link doc-detail__nav-link--next"
        >
          <span class="doc-detail__nav-label">下一篇</span>
          <span class="doc-detail__nav-title">{{ docNav.next.title }}</span>
        </RouterLink>
      </nav>
    </article>
  </ContentLayout>

  <div v-if="mobileTocOpen" class="doc-detail__overlay" @click="mobileTocOpen = false" />
</template>

<style scoped lang="scss">
.doc-detail {
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

  &__loading {
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
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
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
