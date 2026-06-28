import docsData from '@/mock/docs.json'
import docsContent from '@/mock/docs-content.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type { ApiResponse, Article, DocNav, DocsQueryParams, PaginatedData, ArticleSavePayload } from '@/types'
import { mockDelay, successResponse } from '@/utils'

const contentMap = docsContent as Record<string, string>

function getAllDocs(): Article[] {
  return (docsData.list as Article[]).map((doc) => ({
    ...doc,
    content: contentMap[doc.slug] ?? '',
  }))
}

function filterDocs(docs: Article[], params: DocsQueryParams): Article[] {
  let result = [...docs]

  if (params.category) {
    result = result.filter((d) => d.categorySlug === params.category)
  }

  if (params.tag) {
    result = result.filter((d) => d.tags.some((t) => t.toLowerCase() === params.tag?.toLowerCase()))
  }

  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    result = result.filter(
      (d) =>
        d.title.toLowerCase().includes(kw) ||
        d.summary.toLowerCase().includes(kw) ||
        d.tags.some((t) => t.toLowerCase().includes(kw)),
    )
  }

  return result.sort(
    (a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime(),
  )
}

function encodeSlug(slug: string): string {
  return encodeURIComponent(slug)
}

export async function fetchDocsList(
  params: DocsQueryParams = {},
): Promise<ApiResponse<PaginatedData<Article>>> {
  if (!USE_MOCK) {
    return request.get('/docs/list', { params })
  }

  await mockDelay()
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 10
  const filtered = filterDocs(getAllDocs(), params)
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize).map(({ content: _, ...rest }) => rest)

  return successResponse({
    list: list as Article[],
    total: filtered.length,
    page,
    pageSize,
  })
}

export async function fetchLatestDocs(limit = 3): Promise<ApiResponse<Article[]>> {
  if (!USE_MOCK) {
    return request.get('/docs/latest', { params: { limit } })
  }

  await mockDelay()
  const list = getAllDocs()
    .sort((a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime())
    .slice(0, limit)
    .map(({ content: _, ...rest }) => rest)
  return successResponse(list as Article[])
}

export async function fetchDocBySlug(slug: string): Promise<ApiResponse<Article | null>> {
  if (!USE_MOCK) {
    return request.get(`/docs/${encodeSlug(slug)}`)
  }

  await mockDelay()
  const doc = getAllDocs().find((item) => item.slug === slug) ?? null
  return successResponse(doc)
}

export async function fetchDocNav(slug: string): Promise<ApiResponse<DocNav>> {
  if (!USE_MOCK) {
    return request.get(`/docs/${encodeSlug(slug)}/nav`)
  }

  await mockDelay(100)
  const sorted = getAllDocs().sort(
    (a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime(),
  )
  const index = sorted.findIndex((d) => d.slug === slug)

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

export async function fetchAllDocSlugs(): Promise<ApiResponse<string[]>> {
  if (!USE_MOCK) {
    const res = await fetchDocsList({ page: 1, pageSize: 1000 })
    if (res.code === 0) {
      return successResponse(res.data.list.map((item) => item.slug))
    }
    return successResponse([])
  }

  await mockDelay(50)
  return successResponse(getAllDocs().map((d) => d.slug))
}

export async function createDoc(payload: ArticleSavePayload): Promise<ApiResponse<Article>> {
  return request.post('/docs', payload)
}

export async function updateDoc(
  slug: string,
  payload: ArticleSavePayload,
): Promise<ApiResponse<Article>> {
  return request.put(`/docs/${encodeSlug(slug)}`, payload)
}

export async function deleteDoc(slug: string): Promise<ApiResponse<null>> {
  return request.delete(`/docs/${encodeSlug(slug)}`)
}
