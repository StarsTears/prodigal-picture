// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** analyzeSpaceCategory POST /api/space/analyze/category */
export async function analyzeSpaceCategoryUsingPost(
  body: API.SpaceCategoryAnalyzeDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListSpaceCategoryAnalyzeVO_>('/api/space/analyze/category', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** analyzeSpaceRank POST /api/space/analyze/rank */
export async function analyzeSpaceRankUsingPost(
  body: API.SpaceRankAnalyzeDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListSpace_>('/api/space/analyze/rank', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** analyzeSpaceSize POST /api/space/analyze/size */
export async function analyzeSpaceSizeUsingPost(
  body: API.SpaceSizeAnalyzeDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListSpaceSizeAnalyzeVO_>('/api/space/analyze/size', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** analyzeSpaceTag POST /api/space/analyze/tag */
export async function analyzeSpaceTagUsingPost(
  body: API.SpaceTagAnalyzeDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListSpaceTagAnalyzeVO_>('/api/space/analyze/tag', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** analyzeSpaceUsage POST /api/space/analyze/usage */
export async function analyzeSpaceUsageUsingPost(
  body: API.SpaceUsageAnalyzeDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultSpaceUsageAnalyzeVO_>('/api/space/analyze/usage', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** analyzeSpaceUser POST /api/space/analyze/user */
export async function analyzeSpaceUserUsingPost(
  body: API.SpaceUserAnalyzeDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListSpaceUserAnalyzeVO_>('/api/space/analyze/user', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
