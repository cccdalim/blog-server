import recipesData from '@/mock/recipes.json'
import recipesContent from '@/mock/recipes-content.json'
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

const contentMap = recipesContent as Record<string, string>

function getAllRecipes(): Article[] {
  return (recipesData.list as Article[]).map((recipe) => ({
    ...recipe,
    content: contentMap[recipe.slug] ?? '',
  }))
}

function filterRecipes(recipes: Article[], params: DocsQueryParams): Article[] {
  let result = [...recipes]

  if (params.category) {
    result = result.filter((r) => r.categorySlug === params.category)
  }

  if (params.tag) {
    result = result.filter((r) =>
      r.tags.some((t) => t.toLowerCase() === params.tag?.toLowerCase()),
    )
  }

  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    result = result.filter(
      (r) =>
        r.title.toLowerCase().includes(kw) ||
        r.summary.toLowerCase().includes(kw) ||
        r.tags.some((t) => t.toLowerCase().includes(kw)),
    )
  }

  return result.sort(
    (a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime(),
  )
}

function stripContent(recipe: Article): Article {
  const { content: _, ...rest } = recipe
  return rest as Article
}

function encodeSlug(slug: string): string {
  return encodeURIComponent(slug)
}

export async function fetchRecipeList(
  params: DocsQueryParams = {},
): Promise<ApiResponse<PaginatedData<Article>>> {
  if (!USE_MOCK) {
    return request.get('/recipes/list', { params })
  }

  await mockDelay()
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 10
  const filtered = filterRecipes(getAllRecipes(), params)
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize).map(stripContent)

  return successResponse({
    list,
    total: filtered.length,
    page,
    pageSize,
  })
}

export async function fetchLatestRecipes(limit = 3): Promise<ApiResponse<Article[]>> {
  if (!USE_MOCK) {
    return request.get('/recipes/latest', { params: { limit } })
  }

  await mockDelay()
  const list = getAllRecipes()
    .sort((a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime())
    .slice(0, limit)
    .map(stripContent)
  return successResponse(list)
}

export async function fetchRecipeBySlug(slug: string): Promise<ApiResponse<Article | null>> {
  if (!USE_MOCK) {
    return request.get(`/recipes/${encodeSlug(slug)}`)
  }

  await mockDelay()
  const recipe = getAllRecipes().find((item) => item.slug === slug) ?? null
  return successResponse(recipe)
}

export async function fetchRecipeNav(slug: string): Promise<ApiResponse<DocNav>> {
  if (!USE_MOCK) {
    return request.get(`/recipes/${encodeSlug(slug)}/nav`)
  }

  await mockDelay(100)
  const sorted = getAllRecipes().sort(
    (a, b) => new Date(b.publishDate).getTime() - new Date(a.publishDate).getTime(),
  )
  const index = sorted.findIndex((r) => r.slug === slug)

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

export async function createRecipe(payload: ArticleSavePayload): Promise<ApiResponse<Article>> {
  return request.post('/recipes', payload)
}

export async function updateRecipe(
  slug: string,
  payload: ArticleSavePayload,
): Promise<ApiResponse<Article>> {
  return request.put(`/recipes/${encodeSlug(slug)}`, payload)
}

export async function deleteRecipe(slug: string): Promise<ApiResponse<null>> {
  return request.delete(`/recipes/${encodeSlug(slug)}`)
}

export async function importRecipeSeed(replace = false): Promise<ApiResponse<{ count: number }>> {
  return request.post('/recipes/seed', null, { params: { replace } })
}
