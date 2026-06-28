/** 分类 */
export interface CategoryItem {
  id: string
  name: string
  slug: string
  count: number
}

/** 标签 */
export interface TagItem {
  id: string
  name: string
  slug: string
  count: number
}

/** 文档查询参数 */
export interface DocsQueryParams {
  page?: number
  pageSize?: number
  category?: string
  tag?: string
  keyword?: string
}

/** Markdown 目录项 */
export interface TocItem {
  id: string
  text: string
  level: number
}

/** 上下篇导航 */
export interface DocNav {
  prev: { slug: string; title: string } | null
  next: { slug: string; title: string } | null
}
