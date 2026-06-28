import aboutData from '@/mock/about.json'
import skillsData from '@/mock/skills.json'
import request from '@/api/request'
import { USE_MOCK } from '@/constants'
import type {
  AboutData,
  AboutPageData,
  ApiResponse,
  LearningPathItem,
  Skill,
  TimelineItem,
} from '@/types'
import { mockDelay, successResponse } from '@/utils'

export async function fetchAbout(): Promise<ApiResponse<AboutData>> {
  if (!USE_MOCK) {
    return request.get('/about')
  }

  await mockDelay(200)
  const { profile, social } = aboutData
  return successResponse({ profile, social } as AboutData)
}

export async function fetchAboutPage(): Promise<ApiResponse<AboutPageData>> {
  if (!USE_MOCK) {
    return request.get('/about/page')
  }

  await mockDelay()
  return successResponse(aboutData as AboutPageData)
}

export async function fetchSkills(): Promise<ApiResponse<Skill[]>> {
  if (!USE_MOCK) {
    return request.get('/about/skills')
  }

  await mockDelay(200)
  return successResponse(skillsData.skills as Skill[])
}

export async function fetchAboutTimeline(): Promise<ApiResponse<TimelineItem[]>> {
  if (!USE_MOCK) {
    return request.get('/about/timeline')
  }

  await mockDelay(100)
  return successResponse((aboutData as AboutPageData).timeline)
}

export async function fetchLearningPath(): Promise<ApiResponse<LearningPathItem[]>> {
  if (!USE_MOCK) {
    return request.get('/about/learning-path')
  }

  await mockDelay(100)
  return successResponse((aboutData as AboutPageData).learningPath)
}

export async function updateAboutPage(payload: AboutPageData): Promise<ApiResponse<AboutPageData>> {
  return request.put('/about/page', payload)
}

export async function updateAboutSkills(skills: Skill[]): Promise<ApiResponse<Skill[]>> {
  return request.put('/about/skills', { skills })
}

export async function importAboutSeed(replace = false): Promise<ApiResponse<{ count: number }>> {
  return request.post('/about/seed', null, { params: { replace } })
}
