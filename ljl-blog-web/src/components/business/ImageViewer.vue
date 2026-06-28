<script setup lang="ts">
interface Props {
  images: string[]
  alt?: string
}

const props = withDefaults(defineProps<Props>(), {
  alt: '项目截图',
})

const previewList = props.images
</script>

<template>
  <div class="image-viewer">
    <el-image
      v-for="(url, index) in images"
      :key="url"
      :src="url"
      :alt="`${alt} ${index + 1}`"
      :preview-src-list="previewList"
      :initial-index="index"
      fit="cover"
      class="image-viewer__item img-zoom"
      loading="lazy"
    />
  </div>
</template>

<style scoped lang="scss">
.image-viewer {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;

  @include tablet {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
  }

  &__item {
    width: 100%;
    aspect-ratio: 16 / 10;
    border-radius: $radius-md;
    border: 2px solid var(--pixel-border);
    overflow: hidden;
    cursor: zoom-in;
    transition: border-color var(--transition-fast);

    &:hover {
      border-color: var(--pixel-accent);
    }

    :deep(.el-image__inner) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}
</style>
