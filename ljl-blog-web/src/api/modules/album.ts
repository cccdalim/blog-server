import travelData from '@/mock/travel.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type {
  AlbumFilterOption,
  AlbumQueryParams,
  AlbumTimelineItem,
  ApiResponse,
  PaginatedData,
  Photo,
  PhotoSavePayload,
} from '@/types'
import { mockDelay, successResponse } from '@/utils'

function getAllPhotos(): Photo[] {
  return travelData.list as Photo[]
}

function filterPhotos(photos: Photo[], params: AlbumQueryParams): Photo[] {
  let result = [...photos]

  if (params.category) {
    result = result.filter((p) => p.category === params.category)
  }
  if (params.country) {
    result = result.filter((p) => p.country === params.country)
  }
  if (params.city) {
    result = result.filter((p) => p.city === params.city)
  }

  return result.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
}

function countBy<T>(items: T[], keyFn: (item: T) => string): AlbumFilterOption[] {
  const map = new Map<string, number>()
  for (const item of items) {
    const key = keyFn(item)
    map.set(key, (map.get(key) ?? 0) + 1)
  }
  return Array.from(map.entries())
    .map(([value, count]) => ({ label: value, value, count }))
    .sort((a, b) => b.count - a.count)
}

function buildTimeline(photos: Photo[]): AlbumTimelineItem[] {
  return photos.map((p) => ({
    date: p.date,
    title: `${p.city} · ${p.country}`,
    description: p.title,
  }))
}

export async function fetchPhotoList(
  params: AlbumQueryParams = {},
): Promise<ApiResponse<PaginatedData<Photo>>> {
  if (!USE_MOCK) {
    return request.get('/album/list', { params })
  }

  await mockDelay()
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 12
  const filtered = filterPhotos(getAllPhotos(), params)
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return successResponse({
    list,
    total: filtered.length,
    page,
    pageSize,
  })
}

export async function fetchRecentPhotos(limit = 4): Promise<ApiResponse<Photo[]>> {
  if (!USE_MOCK) {
    return request.get('/album/recent', { params: { limit } })
  }

  await mockDelay()
  const list = getAllPhotos()
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
    .slice(0, limit)
  return successResponse(list)
}

export async function fetchAlbumFilters(
  params: Pick<AlbumQueryParams, 'category' | 'country'> = {},
): Promise<
  ApiResponse<{
    categories: AlbumFilterOption[]
    countries: AlbumFilterOption[]
    cities: AlbumFilterOption[]
  }>
> {
  if (!USE_MOCK) {
    return request.get('/album/filters', { params })
  }

  await mockDelay(100)
  const photos = getAllPhotos()
  const scoped = filterPhotos(photos, params)
  return successResponse({
    categories: countBy(photos, (p) => p.category),
    countries: countBy(photos, (p) => p.country),
    cities: countBy(scoped, (p) => p.city),
  })
}

export async function fetchAlbumTimeline(
  params: AlbumQueryParams = {},
): Promise<ApiResponse<AlbumTimelineItem[]>> {
  if (!USE_MOCK) {
    return request.get('/album/timeline', { params })
  }

  await mockDelay(100)
  const filtered = filterPhotos(getAllPhotos(), params)
  return successResponse(buildTimeline(filtered))
}

export async function createPhoto(payload: PhotoSavePayload): Promise<ApiResponse<Photo>> {
  return request.post('/album', payload)
}

export async function updatePhoto(
  id: string,
  payload: PhotoSavePayload,
): Promise<ApiResponse<Photo>> {
  return request.put(`/album/${id}`, payload)
}

export async function deletePhoto(id: string): Promise<ApiResponse<null>> {
  return request.delete(`/album/${id}`)
}

export async function importAlbumSeed(replace = false): Promise<ApiResponse<{ count: number }>> {
  return request.post('/album/seed', null, { params: { replace } })
}
