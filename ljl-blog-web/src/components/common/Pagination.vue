<script setup lang="ts">
defineOptions({ name: 'AppPagination' })

interface Props {
  current: number
  total: number
}

defineProps<Props>()

const emit = defineEmits<{
  change: [page: number]
}>()

function goTo(page: number) {
  emit('change', page)
}
</script>

<template>
  <nav v-if="total > 1" class="pagination" aria-label="分页导航">
    <button
      type="button"
      class="pagination__btn"
      :disabled="current <= 1"
      @click="goTo(current - 1)"
    >
      ← 上一页
    </button>

    <span class="pagination__info">{{ current }} / {{ total }}</span>

    <button
      type="button"
      class="pagination__btn"
      :disabled="current >= total"
      @click="goTo(current + 1)"
    >
      下一页 →
    </button>
  </nav>
</template>

<style scoped lang="scss">
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-lg;
  padding-block: $spacing-lg;

  &__btn {
    @include pixel-button;
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
    border-radius: $radius-sm;

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
      transform: none;
      box-shadow: none;
    }
  }

  &__info {
    font-size: $font-size-sm;
    color: var(--color-text-secondary);
    font-family: $font-family-pixel;
  }
}
</style>
