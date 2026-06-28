<script setup lang="ts">
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import ContactLinks from '@/components/business/ContactLinks.vue'
import LearningPath from '@/components/business/LearningPath.vue'
import SkillTree from '@/components/business/SkillTree.vue'
import Timeline from '@/components/business/Timeline.vue'
import { useRevealAfterLoad } from '@/composables/useRevealAfterLoad'
import { useSettingStore } from '@/stores/setting'

const settingStore = useSettingStore()
const { aboutPage, skills, loading } = storeToRefs(settingStore)

useRevealAfterLoad(loading)

onMounted(() => {
  settingStore.loadAboutPage()
})
</script>

<template>
  <div class="about-page">
    <div v-if="loading" class="about-page__loading container">加载中...</div>

    <template v-else-if="aboutPage">
      <!-- 个人介绍 -->
      <section class="about-page__hero reveal-hero">
        <div class="about-page__hero-inner container">
          <div class="about-page__avatar-wrap">
            <img
              :src="aboutPage.profile.avatar"
              :alt="aboutPage.profile.name"
              class="about-page__avatar"
            />
          </div>
          <div class="about-page__info">
            <h1 class="about-page__name">{{ aboutPage.profile.name }}</h1>
            <p class="about-page__title">{{ aboutPage.profile.title }}</p>
            <p class="about-page__tagline">{{ aboutPage.profile.tagline }}</p>
            <p class="about-page__bio">{{ aboutPage.profile.bio }}</p>
          </div>
        </div>
      </section>

      <div class="container">
        <!-- 技能树 -->
        <section v-if="skills.length" class="about-page__section reveal">
          <h2 class="section-title">技能树</h2>
          <SkillTree :skills="skills" />
        </section>

        <!-- 学习路线 -->
        <section v-if="aboutPage.learningPath.length" class="about-page__section reveal reveal-delay-1">
          <h2 class="section-title">学习路线</h2>
          <LearningPath :items="aboutPage.learningPath" />
        </section>

        <!-- 个人时间轴 -->
        <section v-if="aboutPage.timeline.length" class="about-page__section reveal reveal-delay-2">
          <h2 class="section-title">成长时间轴</h2>
          <Timeline :items="aboutPage.timeline" />
        </section>

        <!-- 联系方式 -->
        <section class="about-page__section about-page__contact reveal reveal-delay-3">
          <h2 class="section-title">联系方式</h2>
          <p class="about-page__contact-desc">欢迎交流技术、项目合作或交个朋友</p>
          <ContactLinks :social="aboutPage.social" />
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.about-page {
  padding-bottom: $spacing-2xl;

  &__loading {
    text-align: center;
    padding: $spacing-3xl;
    color: var(--color-text-muted);
  }

  &__hero {
    background: var(--color-bg-card);
    border-bottom: 2px solid var(--color-border);
    padding-block: $spacing-2xl;
    margin-bottom: $spacing-xl;
  }

  &__hero-inner {
    display: flex;
    align-items: center;
    gap: $spacing-xl;

    @include mobile {
      flex-direction: column;
      text-align: center;
    }
  }

  &__avatar-wrap {
    flex-shrink: 0;
  }

  &__avatar {
    width: 120px;
    height: 120px;
    border-radius: $radius-md;
    @include pixel-border(var(--pixel-accent-strong));
    object-fit: cover;
    background: var(--color-bg-elevated);
  }

  &__name {
    font-size: $font-size-3xl;
    font-weight: 800;
    color: var(--color-text-primary);
    margin-bottom: $spacing-xs;

    @include mobile {
      font-size: $font-size-2xl;
    }
  }

  &__title {
    font-size: $font-size-lg;
    color: var(--pixel-accent);
    font-weight: 600;
    margin-bottom: $spacing-xs;
  }

  &__tagline {
    font-family: $font-family-pixel;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
    margin-bottom: $spacing-md;
  }

  &__bio {
    font-size: $font-size-base;
    color: var(--color-text-secondary);
    line-height: 1.8;
    max-width: 560px;
  }

  &__section {
    margin-bottom: $spacing-2xl;
  }

  &__contact {
    padding: $spacing-xl;
    background: var(--color-bg-card);
    border: 1px solid var(--color-border);
    border-radius: $radius-md;
    text-align: center;
  }

  &__contact-desc {
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    margin-bottom: $spacing-lg;
  }
}
</style>
