import { createRouter, createWebHistory } from 'vue-router'
import { updatePageMeta } from '@/utils/meta'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    layout?: 'main' | 'content' | 'minimal'
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/home/index.vue'),
          meta: { title: '首页' },
        },
        {
          path: '/docs',
          name: 'DocsList',
          component: () => import('@/views/docs/index.vue'),
          meta: { title: '学习文档' },
        },
        {
          path: '/docs/:slug',
          name: 'DocDetail',
          component: () => import('@/views/docs/detail.vue'),
          meta: { title: '文档详情', layout: 'content' },
        },
        {
          path: '/blog',
          name: 'BlogList',
          component: () => import('@/views/blog/index.vue'),
          meta: { title: 'Blog' },
        },
        {
          path: '/blog/:slug',
          name: 'BlogDetail',
          component: () => import('@/views/blog/detail.vue'),
          meta: { title: '文章详情', layout: 'content' },
        },
        {
          path: '/recipes',
          name: 'RecipeList',
          component: () => import('@/views/recipes/index.vue'),
          meta: { title: '美食菜谱' },
        },
        {
          path: '/recipes/:slug',
          name: 'RecipeDetail',
          component: () => import('@/views/recipes/detail.vue'),
          meta: { title: '菜谱详情', layout: 'content' },
        },
        {
          path: '/projects',
          name: 'ProjectList',
          component: () => import('@/views/projects/index.vue'),
          meta: { title: '项目中心' },
        },
        {
          path: '/projects/:id',
          name: 'ProjectDetail',
          component: () => import('@/views/projects/detail.vue'),
          meta: { title: '项目详情' },
        },
        {
          path: '/album',
          name: 'Album',
          component: () => import('@/views/album/index.vue'),
          meta: { title: '旅行相册' },
        },
        {
          path: '/about',
          name: 'About',
          component: () => import('@/views/about/index.vue'),
          meta: { title: '关于我' },
        },
      ],
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/login.vue'),
      meta: { title: '管理员登录', layout: 'minimal', guestOnly: true },
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard',
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/dashboard/index.vue'),
          meta: { title: '控制台', requiresAuth: true },
        },
        {
          path: 'blog',
          name: 'AdminBlogList',
          component: () => import('@/views/admin/blog/index.vue'),
          meta: { title: '文章管理', requiresAuth: true },
        },
        {
          path: 'blog/new',
          name: 'AdminBlogCreate',
          component: () => import('@/views/admin/blog/edit.vue'),
          meta: { title: '新建文章', requiresAuth: true },
        },
        {
          path: 'blog/:slug/edit',
          name: 'AdminBlogEdit',
          component: () => import('@/views/admin/blog/edit.vue'),
          meta: { title: '编辑文章', requiresAuth: true },
        },
        {
          path: 'album',
          name: 'AdminAlbum',
          component: () => import('@/views/admin/album/index.vue'),
          meta: { title: '相册管理', requiresAuth: true },
        },
        {
          path: 'docs',
          name: 'AdminDocsList',
          component: () => import('@/views/admin/docs/index.vue'),
          meta: { title: '文档管理', requiresAuth: true },
        },
        {
          path: 'docs/new',
          name: 'AdminDocsCreate',
          component: () => import('@/views/admin/docs/edit.vue'),
          meta: { title: '新建文档', requiresAuth: true },
        },
        {
          path: 'docs/:slug/edit',
          name: 'AdminDocsEdit',
          component: () => import('@/views/admin/docs/edit.vue'),
          meta: { title: '编辑文档', requiresAuth: true },
        },
        {
          path: 'recipes',
          name: 'AdminRecipesList',
          component: () => import('@/views/admin/recipes/index.vue'),
          meta: { title: '菜谱管理', requiresAuth: true },
        },
        {
          path: 'recipes/new',
          name: 'AdminRecipesCreate',
          component: () => import('@/views/admin/recipes/edit.vue'),
          meta: { title: '新建菜谱', requiresAuth: true },
        },
        {
          path: 'recipes/:slug/edit',
          name: 'AdminRecipesEdit',
          component: () => import('@/views/admin/recipes/edit.vue'),
          meta: { title: '编辑菜谱', requiresAuth: true },
        },
        {
          path: 'projects',
          name: 'AdminProjectsList',
          component: () => import('@/views/admin/projects/index.vue'),
          meta: { title: '项目管理', requiresAuth: true },
        },
        {
          path: 'projects/new',
          name: 'AdminProjectsCreate',
          component: () => import('@/views/admin/projects/edit.vue'),
          meta: { title: '新建项目', requiresAuth: true },
        },
        {
          path: 'projects/:id/edit',
          name: 'AdminProjectsEdit',
          component: () => import('@/views/admin/projects/edit.vue'),
          meta: { title: '编辑项目', requiresAuth: true },
        },
        {
          path: 'about',
          name: 'AdminAbout',
          component: () => import('@/views/admin/about/index.vue'),
          meta: { title: '关于页管理', requiresAuth: true },
        },
        {
          path: 'meta',
          name: 'AdminMeta',
          component: () => import('@/views/admin/meta/index.vue'),
          meta: { title: '分类标签管理', requiresAuth: true },
        },
        {
          path: 'maintenance',
          name: 'AdminMaintenance',
          component: () => import('@/views/admin/maintenance/index.vue'),
          meta: { title: '运维工具', requiresAuth: true },
        },
        {
          path: 'account',
          name: 'AdminAccount',
          component: () => import('@/views/admin/account/index.vue'),
          meta: { title: '账号设置', requiresAuth: true },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/404.vue'),
      meta: { title: '404', layout: 'minimal' },
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    // 浏览器前进/后退：恢复滚动位置
    if (savedPosition) return savedPosition
    // 路由切换：滚到顶部
    if (to.path !== from.path) return { top: 0 }
    return false
  },
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      return { name: 'AdminLogin', query: { redirect: to.fullPath } }
    }
    const valid = await authStore.restoreSession()
    if (!valid) {
      return { name: 'AdminLogin', query: { redirect: to.fullPath } }
    }
  }

  if (to.meta.guestOnly && authStore.isLoggedIn) {
    return '/admin/dashboard'
  }
})

router.afterEach((to, from) => {
  const pageTitle = to.meta.title ?? 'Page'
  updatePageMeta(pageTitle, to.fullPath)

  // 点击 Logo / 首页等同路径导航时，scrollBehavior 不触发，需手动回顶
  if (to.path === from.path && to.path === '/') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
})

export default router
