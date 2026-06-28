import projectData from '@/mock/project.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type { ApiResponse, PaginatedData, Project, ProjectQueryParams, ProjectSavePayload } from '@/types'
import { mockDelay, successResponse } from '@/utils'

function getAllProjects(): Project[] {
  return projectData.list as Project[]
}

function filterProjects(projects: Project[], params: ProjectQueryParams): Project[] {
  let result = [...projects]

  if (params.featured) {
    result = result.filter((p) => p.featured)
  }

  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    result = result.filter(
      (p) =>
        p.name.toLowerCase().includes(kw) ||
        p.summary.toLowerCase().includes(kw) ||
        p.techStack.some((t) => t.toLowerCase().includes(kw)),
    )
  }

  return result.sort(
    (a, b) => new Date(b.startDate).getTime() - new Date(a.startDate).getTime(),
  )
}

export async function fetchProjectList(
  params: ProjectQueryParams = {},
): Promise<ApiResponse<PaginatedData<Project>>> {
  if (!USE_MOCK) {
    return request.get('/projects/list', { params })
  }

  await mockDelay()
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 9
  const filtered = filterProjects(getAllProjects(), params)
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return successResponse({
    list,
    total: filtered.length,
    page,
    pageSize,
  })
}

export async function fetchFeaturedProjects(limit = 3): Promise<ApiResponse<Project[]>> {
  if (!USE_MOCK) {
    return request.get('/projects/featured', { params: { limit } })
  }

  await mockDelay()
  const list = getAllProjects().filter((p) => p.featured).slice(0, limit)
  return successResponse(list)
}

export async function fetchProjectById(id: string): Promise<ApiResponse<Project | null>> {
  if (!USE_MOCK) {
    return request.get(`/projects/${id}`)
  }

  await mockDelay()
  const project = getAllProjects().find((item) => item.id === id) ?? null
  return successResponse(project)
}

export async function fetchProjectNav(id: string): Promise<
  ApiResponse<{ prev: { id: string; name: string } | null; next: { id: string; name: string } | null }>
> {
  if (!USE_MOCK) {
    return request.get(`/projects/${id}/nav`)
  }

  await mockDelay(100)
  const sorted = getAllProjects().sort(
    (a, b) => new Date(b.startDate).getTime() - new Date(a.startDate).getTime(),
  )
  const index = sorted.findIndex((p) => p.id === id)

  if (index === -1) {
    return successResponse({ prev: null, next: null })
  }

  const prev = index > 0 ? sorted[index - 1] : null
  const next = index < sorted.length - 1 ? sorted[index + 1] : null

  return successResponse({
    prev: prev ? { id: prev.id, name: prev.name } : null,
    next: next ? { id: next.id, name: next.name } : null,
  })
}

export async function createProject(payload: ProjectSavePayload): Promise<ApiResponse<Project>> {
  return request.post('/projects', payload)
}

export async function updateProject(
  id: string,
  payload: ProjectSavePayload,
): Promise<ApiResponse<Project>> {
  return request.put(`/projects/${id}`, payload)
}

export async function deleteProject(id: string): Promise<ApiResponse<null>> {
  return request.delete(`/projects/${id}`)
}

export async function importProjectSeed(replace = false): Promise<ApiResponse<{ count: number }>> {
  return request.post('/projects/seed', null, { params: { replace } })
}
