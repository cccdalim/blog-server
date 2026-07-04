import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchDocBySlug, fetchDocNav, fetchDocsList, fetchLatestDocs } from '@/api/modules/docs'
import { fetchCategories, fetchTags } from '@/api/modules/meta'
import type { Article, CategoryItem, DocNav, TagItem } from '@/types'

export const useDocsStore = defineStore('docs', () => {
  const latestDocs = ref<Article[]>([])
  const docList = ref<Article[]>([])
  const currentDoc = ref<Article | null>(null)
  const docNav = ref<DocNav>({ prev: null, next: null })
  const categories = ref<CategoryItem[]>([])
  const tags = ref<TagItem[]>([])
  const loading = ref(false)
  const detailLoading = ref(false)

  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(6)

  const activeCategory = ref('')
  const activeTag = ref('')
  const keyword = ref('')

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  async function loadLatest(limit = 3) {
    loading.value = true
    try {
      const res = await fetchLatestDocs(limit)
      if (res.code === 0) {
        latestDocs.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function loadList(page = 1) {
    loading.value = true
    currentPage.value = page
    try {
      const res = await fetchDocsList({
        page,
        pageSize: pageSize.value,
        category: activeCategory.value || undefined,
        tag: activeTag.value || undefined,
        keyword: keyword.value || undefined,
      })
      if (res.code === 0) {
        docList.value = res.data.list
        total.value = res.data.total
      }
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(slug: string) {
    detailLoading.value = true
    currentDoc.value = null
    docNav.value = { prev: null, next: null }
    try {
      const [docRes, navRes] = await Promise.all([fetchDocBySlug(slug), fetchDocNav(slug)])
      if (docRes.code === 0) {
        currentDoc.value = docRes.data
      }
      if (navRes.code === 0) {
        docNav.value = navRes.data
      }
    } finally {
      detailLoading.value = false
    }
  }

  function clearDetail() {
    currentDoc.value = null
    docNav.value = { prev: null, next: null }
  }

  async function loadMeta() {
    const [catRes, tagRes] = await Promise.all([fetchCategories('content'), fetchTags('content')])
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

  return {
    latestDocs,
    docList,
    currentDoc,
    docNav,
    categories,
    tags,
    loading,
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
    clearDetail,
    setCategory,
    setTag,
    setKeyword,
    resetFilters,
  }
})
