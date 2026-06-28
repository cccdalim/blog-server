import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  fetchFeaturedProjects,
  fetchProjectById,
  fetchProjectList,
  fetchProjectNav,
} from '@/api/modules/project'
import type { Project } from '@/types'

interface ProjectNav {
  prev: { id: string; name: string } | null
  next: { id: string; name: string } | null
}

export const useProjectStore = defineStore('project', () => {
  const featuredProjects = ref<Project[]>([])
  const projectList = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const projectNav = ref<ProjectNav>({ prev: null, next: null })
  const loading = ref(false)
  const detailLoading = ref(false)

  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(9)
  const keyword = ref('')

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  async function loadFeatured(limit = 3) {
    loading.value = true
    try {
      const res = await fetchFeaturedProjects(limit)
      if (res.code === 0) {
        featuredProjects.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function loadList(page = 1) {
    loading.value = true
    currentPage.value = page
    try {
      const res = await fetchProjectList({
        page,
        pageSize: pageSize.value,
        keyword: keyword.value || undefined,
      })
      if (res.code === 0) {
        projectList.value = res.data.list
        total.value = res.data.total
      }
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(id: string) {
    detailLoading.value = true
    currentProject.value = null
    projectNav.value = { prev: null, next: null }
    try {
      const [projectRes, navRes] = await Promise.all([
        fetchProjectById(id),
        fetchProjectNav(id),
      ])
      if (projectRes.code === 0) {
        currentProject.value = projectRes.data
      }
      if (navRes.code === 0) {
        projectNav.value = navRes.data
      }
    } finally {
      detailLoading.value = false
    }
  }

  function setKeyword(kw: string) {
    keyword.value = kw
    loadList(1)
  }

  function clearDetail() {
    currentProject.value = null
    projectNav.value = { prev: null, next: null }
  }

  return {
    featuredProjects,
    projectList,
    currentProject,
    projectNav,
    loading,
    detailLoading,
    total,
    currentPage,
    pageSize,
    totalPages,
    keyword,
    loadFeatured,
    loadList,
    loadDetail,
    setKeyword,
    clearDetail,
  }
})
