<script setup lang="ts">
import { onMounted } from 'vue'
import ArticleCard from '@/components/business/ArticleCard.vue'
import { HOME_LIMITS } from '@/constants'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useRecipeStore } from '@/stores/recipe'
import { storeToRefs } from 'pinia'

const recipeStore = useRecipeStore()
const { latestRecipes, loading } = storeToRefs(recipeStore)

useRevealAfterLoad(loading)

onMounted(() => {
  recipeStore.loadLatest(HOME_LIMITS.recipes)
})
</script>

<template>
  <section class="section reveal">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">最近菜谱</h2>
        <RouterLink to="/recipes" class="section-link">查看全部 →</RouterLink>
      </div>

      <div v-if="loading" class="section-loading">加载中...</div>

      <div v-else class="section-grid">
        <ArticleCard
          v-for="(recipe, index) in latestRecipes"
          :key="recipe.id"
          :article="recipe"
          link-prefix="/recipes"
          :class="['reveal', `reveal-delay-${index + 1}`]"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.section-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;

  @include tablet {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
  }
}

.section-loading {
  text-align: center;
  color: var(--color-text-muted);
  padding: $spacing-2xl;
}
</style>
