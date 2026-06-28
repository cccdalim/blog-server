/** 文章 / 学习文档通用结构 */
export interface Article {
  id: string
  slug: string
  title: string
  summary: string
  content?: string
  cover?: string
  category: string
  categorySlug: string
  tags: string[]
  publishDate: string
  readTime?: number
  featured?: boolean
}
