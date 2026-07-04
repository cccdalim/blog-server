<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  router.replace({ name: 'AdminLogin' })
}
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-layout__sidebar">
      <div class="admin-layout__brand">LJL Admin</div>
      <nav class="admin-layout__nav">
        <RouterLink to="/admin/dashboard" class="admin-layout__link">控制台</RouterLink>
        <RouterLink to="/admin/blog" class="admin-layout__link">文章管理</RouterLink>
        <RouterLink to="/admin/docs" class="admin-layout__link">文档管理</RouterLink>
        <RouterLink to="/admin/recipes" class="admin-layout__link">菜谱管理</RouterLink>
        <RouterLink to="/admin/projects" class="admin-layout__link">项目管理</RouterLink>
        <RouterLink to="/admin/about" class="admin-layout__link">关于页管理</RouterLink>
        <RouterLink to="/admin/meta" class="admin-layout__link">分类 / 标签</RouterLink>
        <RouterLink to="/admin/album" class="admin-layout__link">相册管理</RouterLink>
        <RouterLink to="/admin/maintenance" class="admin-layout__link">运维工具</RouterLink>
        <RouterLink to="/admin/account" class="admin-layout__link">账号设置</RouterLink>
        <RouterLink to="/" class="admin-layout__link">返回站点</RouterLink>
      </nav>
      <div class="admin-layout__footer">
        <span>{{ authStore.username }}</span>
        <button type="button" class="admin-layout__logout" @click="logout">退出</button>
      </div>
    </aside>
    <main class="admin-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);

  &__sidebar {
    width: 220px;
    padding: $spacing-lg;
    background: var(--color-bg-card);
    border-right: 2px solid var(--color-border);
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;
  }

  &__brand {
    font-size: $font-size-lg;
    font-weight: 700;
  }

  &__nav {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    flex: 1;
  }

  &__link {
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-md;
    color: var(--color-text-secondary);
    text-decoration: none;

    &.router-link-active {
      background: rgba(60, 133, 39, 0.15);
      color: var(--pixel-accent-strong);
      font-weight: 600;
    }
  }

  &__footer {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    font-size: $font-size-sm;
    color: var(--color-text-muted);
  }

  &__logout {
    align-self: flex-start;
    background: none;
    border: none;
    color: var(--pixel-redstone);
    cursor: pointer;
    padding: 0;
  }

  &__main {
    flex: 1;
    padding: $spacing-xl;
    overflow: auto;
  }
}
</style>
