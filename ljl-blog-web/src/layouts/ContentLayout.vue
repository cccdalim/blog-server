<script setup lang="ts">
/** ContentLayout — 文档/文章详情共用布局，PC/iPad/移动端响应式 */
interface Props {
  showMobileSidebar?: boolean
}

withDefaults(defineProps<Props>(), {
  showMobileSidebar: false,
})
</script>

<template>
  <div class="content-layout">
    <aside
      class="content-layout__sidebar"
      :class="{ 'content-layout__sidebar--mobile-open': showMobileSidebar }"
    >
      <slot name="sidebar" />
    </aside>

    <div class="content-layout__main">
      <slot name="toolbar" />
      <slot />
    </div>
  </div>
</template>

<style scoped lang="scss">
.content-layout {
  display: flex;
  gap: $spacing-lg;
  @include container;
  padding-block: $spacing-lg;

  &__sidebar {
    width: $sidebar-width;
    flex-shrink: 0;
    position: sticky;
    top: calc(var(--navbar-height) + #{$spacing-lg});
    max-height: calc(100vh - var(--navbar-height) - #{$spacing-2xl});
    overflow-y: auto;

    @include mobile {
      display: none;
      position: fixed;
      top: var(--navbar-height);
      left: 0;
      bottom: 0;
      z-index: $z-sidebar;
      width: 280px;
      max-height: none;
      padding: $spacing-md;
      background: var(--color-bg-card);
      border-right: 2px solid var(--color-border);
      box-shadow: var(--shadow-md);

      &--mobile-open {
        display: block;
      }
    }
  }

  &__main {
    flex: 1;
    min-width: 0;
  }
}
</style>
