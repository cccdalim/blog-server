import blogData from '@/mock/blog.json'
import blogContent from '@/mock/blog-content.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type {
  ApiResponse,
  Article,
  ArticleSavePayload,
  DocNav,
  DocsQueryParams,
  PaginatedData,
} from '@/types'
import { mockDelay, successResponse } from '@/utils'

const contentMap = blogContent as Record<string, string>

function getAllBlogs(): Article[] {
  return (blogData.list as Article[]).map((article) => ({
    ...article,
    content: contentMap[article.slug] ?? '',
  }))
}

function filterBlogs(articles: Article[], params: DocsQueryParams): Article[] {
  let result = [...articles]

  if (params.category) {
    result = result.filter((a) => a.categorySlug === params.category)
  }

  if (params.tag) {
    result = result.filter((a) =>
      a.tags.some((t) => t.toLowerCase() === params.tag?.toLowerCase()),
    )
  }

  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    result = result.filter(
      (a) =>
        a.title.toLowerCase().includes(kw) ||
        a.summary.toLowerCase().includes(kw) ||
        a.tags.some((t) => t.toLowerCase().includes(kw)),
    )
  }

  return result.sort(
    (a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime(),
  )
}

function stripContent(article: Article): Article {
  const { content: _, ...rest } = article
  return rest as Article
}

/** 路径中的 slug 必须编码，否则中文 slug 会导致详情 API 404 */
function encodeSlug(slug: string): string {
  return encodeURIComponent(slug)
}

export async function fetchBlogList(
  params: DocsQueryParams = {},
): Promise<ApiResponse<PaginatedData<Article>>> {
  if (!USE_MOCK) {
    return request.get('/blog/list', { params })
  }

  await mockDelay()
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 10
  const filtered = filterBlogs(getAllBlogs(), params)
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize).map(stripContent)

  return successResponse({
    list,
    total: filtered.length,
    page,
    pageSize,
  })
}

export async function fetchLatestBlogs(limit = 3): Promise<ApiResponse<Article[]>> {
  if (!USE_MOCK) {
    return request.get('/blog/latest', { params: { limit } })
  }

  await mockDelay()
  const list = getAllBlogs()
    .sort((a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime())
    .slice(0, limit)
    .map(stripContent)
  return successResponse(list)
}

export async function fetchBlogBySlug(slug: string): Promise<ApiResponse<Article | null>> {
  if (!USE_MOCK) {
    return request.get(`/blog/${encodeSlug(slug)}`)
  }

  await mockDelay()
  const article = getAllBlogs().find((item) => item.slug === slug) ?? null
  return successResponse(article)
}

export async function fetchBlogNav(slug: string): Promise<ApiResponse<DocNav>> {
  if (!USE_MOCK) {
    return request.get(`/blog/${encodeSlug(slug)}/nav`)
  }

  await mockDelay(100)
  const sorted = getAllBlogs().sort(
    (a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime(),
  )
  const index = sorted.findIndex((a) => a.slug === slug)

  if (index === -1) {
    return successResponse({ prev: null, next: null })
  }

  const prev = index > 0 ? sorted[index - 1] : null
  const next = index < sorted.length - 1 ? sorted[index + 1] : null

  return successResponse({
    prev: prev ? { slug: prev.slug, title: prev.title } : null,
    next: next ? { slug: next.slug, title: next.title } : null,
  })
}

export async function createBlog(payload: ArticleSavePayload): Promise<ApiResponse<Article>> {
  return request.post('/blog', payload)
}

export async function updateBlog(
  slug: string,
  payload: ArticleSavePayload,
): Promise<ApiResponse<Article>> {
  return request.put(`/blog/${encodeSlug(slug)}`, payload)
}

export async function deleteBlog(slug: string): Promise<ApiResponse<null>> {
  return request.delete(`/blog/${encodeSlug(slug)}`)
}

export async function importBlogSeed(replace = false): Promise<ApiResponse<{ count: number }>> {
  return request.post('/blog/seed', null, { params: { replace } })
}
