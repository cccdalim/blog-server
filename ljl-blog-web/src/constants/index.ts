import { appEnv } from '@/config/env'

/** 导航菜单 */
export interface NavItem {
  label: string
  path: string
}

export const SITE_NAME = appEnv.siteName
export const SITE_DESCRIPTION = appEnv.siteDescription
export const SITE_URL = appEnv.siteUrl

export const NAV_ITEMS: NavItem[] = [
  { label: '首页', path: '/' },
  { label: '学习文档', path: '/docs' },
  { label: 'Blog', path: '/blog' },
  { label: '美食菜谱', path: '/recipes' },
  { label: '项目中心', path: '/projects' },
  { label: '旅行相册', path: '/album' },
  { label: '关于我', path: '/about' },
]

/** 首页各区块默认展示数量 */
export const HOME_LIMITS = {
  blog: 3,
  docs: 3,
  recipes: 3,
  projects: 3,
  photos: 4,
} as const

/** 是否使用 Mock 数据 */
export const USE_MOCK = appEnv.useMock
