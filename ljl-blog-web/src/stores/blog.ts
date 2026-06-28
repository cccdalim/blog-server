import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  fetchBlogBySlug,
  fetchBlogList,
  fetchBlogNav,
  fetchLatestBlogs,
} from '@/api/modules/blog'
import { fetchCategories, fetchTags } from '@/api/modules/meta'
import type { Article, CategoryItem, DocNav, TagItem } from '@/types'

export const useBlogStore = defineStore('blog', () => {
  const latestArticles = ref<Article[]>([])
  const articleList = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const articleNav = ref<DocNav>({ prev: null, next: null })
  const categories = ref<CategoryItem[]>([])
  const tags = ref<TagItem[]>([])
  const loading = ref(false)
  const listLoading = ref(false)
  const detailLoading = ref(false)

  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(6)

  const activeCategory = ref('')
  const activeTag = ref('')
  const keyword = ref('')

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  let detailRequestId = 0

  async function loadLatest(limit = 3) {
    loading.value = true
    try {
      const res = await fetchLatestBlogs(limit)
      if (res.code === 0) {
        latestArticles.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function loadList(page = 1) {
    listLoading.value = true
    currentPage.value = page
    try {
      const res = await fetchBlogList({
        page,
        pageSize: pageSize.value,
        category: activeCategory.value || undefined,
        tag: activeTag.value || undefined,
        keyword: keyword.value || undefined,
      })
      if (res.code === 0) {
        articleList.value = res.data.list
        total.value = res.data.total
      }
    } finally {
      listLoading.value = false
    }
  }

  async function loadDetail(slug: string) {
    const requestId = ++detailRequestId
    detailLoading.value = true
    currentArticle.value = null
    articleNav.value = { prev: null, next: null }
    try {
      const [articleRes, navRes] = await Promise.all([
        fetchBlogBySlug(slug),
        fetchBlogNav(slug),
      ])
      if (requestId !== detailRequestId) return
      if (articleRes.code === 0) {
        currentArticle.value = articleRes.data
      }
      if (navRes.code === 0) {
        articleNav.value = navRes.data
      }
    } finally {
      if (requestId === detailRequestId) {
        detailLoading.value = false
      }
    }
  }

  async function loadMeta() {
    const [catRes, tagRes] = await Promise.all([fetchCategories(), fetchTags()])
    if (catRes.code === 0) categories.value = catRes.data
    if (tagRes.code === 0) tags.value = tagRes.data
  }

  function setCategory(slug: string) {
    activeCategory.value = activeCategory.value === slug ? '' : slug
    loadList(1)
  }

  function setTag(tag: string) {
    activeTag.value = activeTag.value === tag ? '' : tag
    loadList(1)
  }

  function setKeyword(kw: string) {
    keyword.value = kw
    loadList(1)
  }

  function resetFilters() {
    activeCategory.value = ''
    activeTag.value = ''
    keyword.value = ''
    loadList(1)
  }

  function clearDetail() {
    currentArticle.value = null
    articleNav.value = { prev: null, next: null }
  }

  return {
    latestArticles,
    articleList,
    currentArticle,
    articleNav,
    categories,
    tags,
    loading,
    listLoading,
    detailLoading,
    total,
    currentPage,
    pageSize,
    totalPages,
    activeCategory,
    activeTag,
    keyword,
    loadLatest,
    loadList,
    loadDetail,
    loadMeta,
    setCategory,
    setTag,
    setKeyword,
    resetFilters,
    clearDetail,
  }
})
