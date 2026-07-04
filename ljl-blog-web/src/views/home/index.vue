<script setup lang="ts">
import { onMounted } from 'vue'
import HeroBanner from '@/components/business/HeroBanner.vue'
import { useSettingStore } from '@/stores/setting'
import { storeToRefs } from 'pinia'
import AlbumSection from './components/AlbumSection.vue'
import BlogSection from './components/BlogSection.vue'
import DocsSection from './components/DocsSection.vue'
import ProjectSection from './components/ProjectSection.vue'
import RecipeSection from './components/RecipeSection.vue'

const settingStore = useSettingStore()
const { about, skills } = storeToRefs(settingStore)

onMounted(async () => {
  await Promise.all([settingStore.loadAbout(), settingStore.loadSkills()])
})
</script>

<template>
  <div class="home">
    <HeroBanner :profile="about?.profile ?? null" :skills="skills" />

    <div class="pixel-divider container reveal" />

    <BlogSection />
    <DocsSection />
    <RecipeSection />
    <ProjectSection />
    <AlbumSection />
  </div>
</template>

<style scoped lang="scss">
.home {
  padding-bottom: $spacing-2xl;
}
</style>
