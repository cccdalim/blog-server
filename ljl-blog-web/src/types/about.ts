import type { TimelineItem } from './timeline'

/** 个人资料 */
export interface Profile {
  name: string
  avatar: string
  title: string
  bio: string
  tagline: string
}

/** 社交链接 */
export interface SocialLinks {
  github?: string
  email?: string
  twitter?: string
}

/** 技能项 */
export interface Skill {
  name: string
  level: 'beginner' | 'intermediate' | 'advanced' | 'expert'
  category?: string
}

/** 基础关于数据 — 首页 Hero 使用 */
export interface AboutData {
  profile: Profile
  social: SocialLinks
}

/** 学习路线节点 */
export interface LearningPathItem {
  id: string
  title: string
  description: string
  status: 'completed' | 'in-progress' | 'planned'
  skills: string[]
}

/** 关于页完整数据 */
export interface AboutPageData extends AboutData {
  timeline: TimelineItem[]
  learningPath: LearningPathItem[]
}
