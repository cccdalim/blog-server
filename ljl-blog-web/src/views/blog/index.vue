<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import ArticleCard from '@/components/business/ArticleCard.vue'
import AppTag from '@/components/common/AppTag.vue'
import Pagination from '@/components/common/Pagination.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useBlogStore } from '@/stores/blog'

const route = useRoute()
const router = useRouter()
const blogStore = useBlogStore()
const {
  articleList,
  categories,
  tags,
  listLoading,
  totalPages,
  currentPage,
  activeCategory,
  activeTag,
  keyword,
} = storeToRefs(blogStore)

useRevealAfterLoad(listLoading)

function buildQuery(overrides: Record<string, string | undefined> = {}) {
  const query: Record<string, string> = {}
  const category = 'category' in overrides ? overrides.category : activeCategory.value
  const tag = 'tag' in overrides ? overrides.tag : activeTag.value
  const kw = 'keyword' in overrides ? overrides.keyword : keyword.value
  const pageRaw = 'page' in overrides ? overrides.page : String(currentPage.value || 1)
  const page = pageRaw ?? '1'

  if (category) query.category = category
  if (tag) query.tag = tag
  if (kw) query.keyword = kw
  if (page && page !== '1') query.page = page
  return query
}

async function syncFromRoute() {
  activeCategory.value = (route.query.category as string) || ''
  activeTag.value = (route.query.tag as string) || ''
  keyword.value = (route.query.keyword as string) || ''
  const page = Number(route.query.page) || 1
  await blogStore.loadList(page)
}

function updateRoute(overrides: Record<string, string | undefined>) {
  return router.replace({ path: '/blog', query: buildQuery(overrides) })
}

function handleSetCategory(slug: string) {
  if (!slug) {
    updateRoute({ category: undefined, tag: undefined, page: undefined })
    return
  }
  const next = activeCategory.value === slug ? '' : slug
  updateRoute({ category: next || undefined, page: undefined })
}

function handleSetTag(tag: string) {
  const next = activeTag.value === tag ? '' : tag
  updateRoute({ tag: next || undefined, page: undefined })
}

function onSearch(kw: string) {
  updateRoute({ keyword: kw || undefined, page: undefined })
}

function handleReset() {
  router.replace({ path: '/blog' })
}

function handlePageChange(page: number) {
  updateRoute({ page: String(page) })
}

onMounted(async () => {
  await blogStore.loadMeta()
  await syncFromRoute()
})

watch(
  () => [route.query.category, route.query.tag, route.query.keyword, route.query.page] as const,
  () => {
    if (route.name !== 'BlogList') return
    syncFromRoute()
  },
)
</script>

<template>
  <div class="blog-page">
    <header class="blog-page__header container reveal">
      <h1 class="blog-page__title">Blog</h1>
      <p class="blog-page__desc">技术博客，分享前端开发与工程化实践</p>
    </header>

    <div class="blog-page__filters container reveal reveal-delay-1">
      <SearchBar
        :model-value="keyword"
        placeholder="搜索文章标题、标签..."
        @search="onSearch"
      />

      <div class="blog-page__categories">
        <button
          type="button"
          class="blog-page__filter-btn"
          :class="{ 'blog-page__filter-btn--active': !activeCategory }"
          @click="handleSetCategory('')"
        >
          全部
        </button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="blog-page__filter-btn"
          :class="{ 'blog-page__filter-btn--active': activeCategory === cat.slug }"
          @click="handleSetCategory(cat.slug)"
        >
          {{ cat.name }}
        </button>
      </div>

      <div v-if="tags.length" class="blog-page__tags">
        <button
          v-for="tag in tags.slice(0, 8)"
          :key="tag.id"
          type="button"
          class="blog-page__tag-btn"
          @click="handleSetTag(tag.name)"
        >
          <AppTag
            :label="tag.name"
            :variant="activeTag === tag.name ? 'grass' : 'outline'"
            size="md"
          />
        </button>
      </div>
    </div>

    <section class="blog-page__list container">
      <div v-if="listLoading" class="blog-page__loading">加载中...</div>

      <div v-else-if="!articleList.length" class="blog-page__empty">
        <p>没有找到匹配的文章</p>
        <button type="button" class="blog-page__reset" @click="handleReset">
          清除筛选
        </button>
      </div>

      <div v-else class="blog-page__grid">
        <ArticleCard
          v-for="(article, index) in articleList"
          :key="article.id"
          :article="article"
          link-prefix="/blog"
          :class="['reveal', `reveal-delay-${Math.min(index + 1, 12)}`]"
        />
      </div>

      <Pagination
        class="reveal"
        :current="currentPage"
        :total="totalPages"
        @change="handlePageChange"
      />
    </section>
  </div>
</template>

<style scoped lang="scss">
.blog-page {
  padding-bottom: $spacing-2xl;

  &__header {
    padding-block: $spacing-xl $spacing-lg;
  }

  &__title {
    @include section-title;
    font-size: $font-size-2xl;
    margin-bottom: $spacing-sm;
  }

  &__desc {
    color: var(--color-text-secondary);
    font-size: $font-size-base;
  }

  &__filters {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
    margin-bottom: $spacing-xl;
  }

  &__categories {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__filter-btn {
    padding: $spacing-xs $spacing-md;
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-sm;
    transition:
      color var(--transition-fast),
      border-color var(--transition-fast),
      background var(--transition-fast);

    &:hover {
      color: var(--pixel-accent);
      border-color: var(--pixel-accent);
    }

    &--active {
      color: var(--pixel-accent);
      border-color: var(--pixel-accent-strong);
      background: var(--pixel-grass-bg-subtle);
    }
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__tag-btn {
    cursor: pointer;
    background: none;
    border: none;
    padding: 0;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $spacing-lg;
    margin-bottom: $spacing-lg;

    @include tablet {
      grid-template-columns: repeat(2, 1fr);
    }

    @include mobile {
      grid-template-columns: 1fr;
    }
  }

  &__loading,
  &__empty {
    text-align: center;
    padding: $spacing-3xl;
    color: var(--color-text-muted);
  }

  &__reset {
    margin-top: $spacing-md;
    color: var(--pixel-accent);
    font-size: $font-size-sm;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
