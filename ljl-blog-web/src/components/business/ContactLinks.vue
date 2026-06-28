<script setup lang="ts">
import AppButton from '@/components/common/AppButton.vue'
import type { SocialLinks } from '@/types'

interface Props {
  social: SocialLinks
}

defineProps<Props>()
</script>

<template>
  <div class="contact-links">
    <a
      v-if="social.github"
      :href="social.github"
      target="_blank"
      rel="noopener noreferrer"
      class="contact-links__item"
    >
      <AppButton variant="primary" size="lg">
        GitHub
      </AppButton>
    </a>

    <a
      v-if="social.email"
      :href="`mailto:${social.email}`"
      class="contact-links__item"
    >
      <AppButton variant="secondary" size="lg">
        {{ social.email }}
      </AppButton>
    </a>

    <a
      v-if="social.twitter"
      :href="social.twitter"
      target="_blank"
      rel="noopener noreferrer"
      class="contact-links__item"
    >
      <AppButton variant="ghost" size="lg">
        Twitter / X
      </AppButton>
    </a>
  </div>
</template>

<style scoped lang="scss">
.contact-links {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;

  &__item {
    color: inherit;

    &:hover {
      color: inherit;
    }
  }
}
</style>
