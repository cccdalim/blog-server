/** 项目时间轴节点 */
export interface ProjectTimelineItem {
  date: string
  title: string
  description: string
}

/** 项目展示 */
export interface Project {
  id: string
  name: string
  description: string
  summary: string
  techStack: string[]
  cover: string
  gitUrl?: string
  demoUrl?: string
  featured?: boolean
  startDate: string
  endDate?: string | null
  /** 详情页扩展字段 */
  devProcess?: string[]
  screenshots?: string[]
  timeline?: ProjectTimelineItem[]
}

/** 项目查询参数 */
export interface ProjectQueryParams {
  page?: number
  pageSize?: number
  keyword?: string
  featured?: boolean
}
