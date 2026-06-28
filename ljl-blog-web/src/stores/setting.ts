import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchAbout, fetchAboutPage, fetchSkills } from '@/api/modules/about'
import type { AboutData, AboutPageData, Skill } from '@/types'

export const useSettingStore = defineStore('setting', () => {
  const about = ref<AboutData | null>(null)
  const aboutPage = ref<AboutPageData | null>(null)
  const skills = ref<Skill[]>([])
  const loading = ref(false)

  async function loadAbout() {
    loading.value = true
    try {
      const res = await fetchAbout()
      if (res.code === 0) {
        about.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  async function loadAboutPage() {
    loading.value = true
    try {
      const [pageRes, skillsRes] = await Promise.all([fetchAboutPage(), fetchSkills()])
      if (pageRes.code === 0) {
        aboutPage.value = pageRes.data
      }
      if (skillsRes.code === 0) {
        skills.value = skillsRes.data
      }
    } finally {
      loading.value = false
    }
  }

  async function loadSkills() {
    const res = await fetchSkills()
    if (res.code === 0) {
      skills.value = res.data
    }
  }

  return { about, aboutPage, skills, loading, loadAbout, loadAboutPage, loadSkills }
})
