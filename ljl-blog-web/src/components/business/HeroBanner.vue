<script setup lang="ts">
import AppButton from '@/components/common/AppButton.vue'
import AppTag from '@/components/common/AppTag.vue'
import type { Profile, Skill } from '@/types'

interface Props {
  profile: Profile | null
  skills: Skill[]
}

defineProps<Props>()
</script>

<template>
  <section class="hero reveal-hero">
    <div class="hero__inner container">
      <div class="hero__content">
        <div class="hero__avatar-wrap">
          <img
            v-if="profile?.avatar"
            :src="profile.avatar"
            :alt="profile.name"
            class="hero__avatar"
          />
        </div>

        <h1 class="hero__title">
          {{ profile?.name ?? 'LJL' }}
        </h1>
        <p class="hero__subtitle">{{ profile?.title }}</p>
        <p class="hero__tagline">{{ profile?.tagline }}</p>
        <p class="hero__bio">{{ profile?.bio }}</p>

        <div v-if="skills.length" class="hero__skills">
          <AppTag
            v-for="skill in skills"
            :key="skill.name"
            :label="skill.name"
            variant="grass"
          />
        </div>

        <div class="hero__actions">
          <RouterLink to="/blog">
            <AppButton variant="primary" size="lg">阅读博客</AppButton>
          </RouterLink>
          <RouterLink to="/about">
            <AppButton variant="secondary" size="lg">关于我</AppButton>
          </RouterLink>
        </div>
      </div>

      <div class="hero__decoration" aria-hidden="true">
        <div class="hero__block hero__block--grass" />
        <div class="hero__block hero__block--wood" />
        <div class="hero__block hero__block--stone" />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.hero {
  padding-block: $spacing-3xl $spacing-2xl;
  position: relative;
  overflow: hidden;

  @include mobile {
    padding-block: $spacing-2xl $spacing-xl;
  }

  &__inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-xl;
  }

  &__content {
    flex: 1;
    max-width: 640px;
  }

  &__avatar-wrap {
    margin-bottom: $spacing-lg;
  }

  &__avatar {
    width: 80px;
    height: 80px;
    border-radius: $radius-md;
    @include pixel-border(var(--pixel-accent-strong));
    object-fit: cover;
  }

  &__title {
    font-size: $font-size-3xl;
    font-weight: 800;
    color: var(--color-text-primary);
    line-height: 1.2;
    margin-bottom: $spacing-sm;

    @include mobile {
      font-size: $font-size-2xl;
    }
  }

  &__subtitle {
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
    margin-bottom: $spacing-lg;
    line-height: 1.7;
  }

  &__skills {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    margin-bottom: $spacing-xl;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
  }

  &__decoration {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
    flex-shrink: 0;

    @include mobile {
      display: none;
    }
  }

  &__block {
    width: 64px;
    height: 64px;
    border-radius: $radius-sm;
    @include pixel-border;

    &--grass {
      background: var(--pixel-grass);
      border-color: var(--pixel-grass-dark);
    }

    &--wood {
      background: var(--pixel-wood);
      border-color: var(--pixel-wood-dark);
      margin-left: $spacing-lg;
    }

    &--stone {
      background: var(--pixel-stone);
      border-color: var(--pixel-stone-dark);
      margin-left: $spacing-xl;
    }
  }
}
</style>
