// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** addSpaceUser POST /api/spaceUser/add */
export async function addSpaceUserUsingPost(
  body: API.SpaceUserAddDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultLong_>('/api/spaceUser/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteSpaceUser POST /api/spaceUser/delete */
export async function deleteSpaceUserUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/spaceUser/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editSpaceUser POST /api/spaceUser/edit */
export async function editSpaceUserUsingPost(
  body: API.SpaceUserEditDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/spaceUser/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceUser GET /api/spaceUser/get */
export async function getSpaceUserUsingGet(
  body: API.SpaceUserQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultSpaceUser_>('/api/spaceUser/get', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listMyTeamSpace POST /api/spaceUser/list/my */
export async function listMyTeamSpaceUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResultListSpaceUserVO_>('/api/spaceUser/list/my', {
    method: 'POST',
    ...(options || {}),
  })
}

/** listSpaceUserVOByPage POST /api/spaceUser/list/page */
export async function listSpaceUserVoByPageUsingPost(
  body: API.SpaceUserQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPageSpaceUserVO_>('/api/spaceUser/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
