import type { TagScope } from './docs'

export interface LoginResponse {
  token: string
  username: string
}

export interface UserInfo {
  username: string
}

export interface ArticleSavePayload {
  slug: string
  title: string
  summary: string
  content: string
  cover?: string
  categorySlug: string
  tags: string[]
  publishDate: string
  readTime?: number
  featured?: boolean
}

export interface PhotoSavePayload {
  url: string
  title: string
  city: string
  country: string
  category: string
  date: string
  width?: number
  height?: number
}

export interface ProjectSavePayload {
  name: string
  description: string
  summary: string
  techStack: string[]
  cover?: string
  gitUrl?: string
  demoUrl?: string
  featured?: boolean
  startDate: string
  endDate?: string | null
  devProcess: string[]
  screenshots: string[]
  timeline: import('./project').ProjectTimelineItem[]
}

export interface MetaSavePayload {
  name: string
  slug: string
  scope?: TagScope
}
