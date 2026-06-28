import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  fetchAlbumFilters,
  fetchAlbumTimeline,
  fetchPhotoList,
  fetchRecentPhotos,
} from '@/api/modules/album'
import type { AlbumFilterOption, AlbumTimelineItem, Photo } from '@/types'

export const useAlbumStore = defineStore('album', () => {
  const recentPhotos = ref<Photo[]>([])
  const photoList = ref<Photo[]>([])
  const categories = ref<AlbumFilterOption[]>([])
  const countries = ref<AlbumFilterOption[]>([])
  const cities = ref<AlbumFilterOption[]>([])
  const countryCities = ref<AlbumFilterOption[]>([])
  const timeline = ref<AlbumTimelineItem[]>([])
  const loading = ref(false)

  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(12)

  const activeCategory = ref('')
  const activeCountry = ref('')
  const activeCity = ref('')

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  const previewUrls = computed(() => photoList.value.map((p) => p.url))

  /** 当前国家下的城市列表（来自 API，非 Mock 静态数据） */
  const filteredCities = computed(() => {
    if (!activeCountry.value) return cities.value
    return countryCities.value
  })

  const queryParams = computed(() => ({
    category: activeCategory.value || undefined,
    country: activeCountry.value || undefined,
    city: activeCity.value || undefined,
  }))

  async function loadRecent(limit = 4) {
    loading.value = true
    try {
      const res = await fetchRecentPhotos(limit)
      if (res.code === 0) {
        recentPhotos.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function loadFilters() {
    const res = await fetchAlbumFilters({
      category: activeCategory.value || undefined,
      country: activeCountry.value || undefined,
    })
    if (res.code === 0) {
      categories.value = res.data.categories
      countries.value = res.data.countries
      cities.value = res.data.cities
      if (activeCountry.value) {
        countryCities.value = res.data.cities
      }
    }
  }

  async function loadCountryCities() {
    if (!activeCountry.value) {
      countryCities.value = []
      return
    }
    const res = await fetchAlbumFilters({
      category: activeCategory.value || undefined,
      country: activeCountry.value,
    })
    if (res.code === 0) {
      countryCities.value = res.data.cities
    }
  }

  async function loadTimeline() {
    const res = await fetchAlbumTimeline(queryParams.value)
    if (res.code === 0) {
      timeline.value = res.data
    }
  }

  async function loadList(page = 1) {
    loading.value = true
    currentPage.value = page
    try {
      const res = await fetchPhotoList({
        page,
        pageSize: pageSize.value,
        ...queryParams.value,
      })
      if (res.code === 0) {
        photoList.value = res.data.list
        total.value = res.data.total
      }
      await loadTimeline()
    } finally {
      loading.value = false
    }
  }

  function setCategory(value: string) {
    activeCategory.value = activeCategory.value === value ? '' : value
    activeCity.value = ''
    void loadCountryCities().then(() => loadList(1))
  }

  async function setCountry(value: string) {
    activeCountry.value = activeCountry.value === value ? '' : value
    activeCity.value = ''
    await loadCountryCities()
    await loadList(1)
  }

  function setCity(value: string) {
    activeCity.value = activeCity.value === value ? '' : value
    loadList(1)
  }

  function resetFilters() {
    activeCategory.value = ''
    activeCountry.value = ''
    activeCity.value = ''
    countryCities.value = []
    loadList(1)
  }

  return {
    recentPhotos,
    photoList,
    categories,
    countries,
    cities,
    timeline,
    loading,
    total,
    currentPage,
    pageSize,
    totalPages,
    previewUrls,
    filteredCities,
    activeCategory,
    activeCountry,
    activeCity,
    loadRecent,
    loadFilters,
    loadList,
    setCategory,
    setCountry,
    setCity,
    resetFilters,
  }
})
