// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** addSpace POST /api/space/add */
export async function addSpaceUsingPost(body: API.SpaceAddDto, options?: { [key: string]: any }) {
  return request<API.BaseResultLong_>('/api/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteSpace POST /api/space/delete */
export async function deleteSpaceUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/space/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editPicture POST /api/space/edit */
export async function editPictureUsingPost1(
  body: API.SpaceEditDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/space/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceByID GET /api/space/get */
export async function getSpaceByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceByIDUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultSpace_>('/api/space/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceVOByID GET /api/space/get/vo */
export async function getSpaceVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVOByIDUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultSpaceVO_>('/api/space/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listSpaceLevel GET /api/space/list/level */
export async function listSpaceLevelUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultListSpaceLevel_>('/api/space/list/level', {
    method: 'GET',
    ...(options || {}),
  })
}

/** listSpaceByPage POST /api/space/list/page */
export async function listSpaceByPageUsingPost(
  body: API.SpaceQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPageSpace_>('/api/space/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listSpaceVOByPage POST /api/space/list/page/vo */
export async function listSpaceVoByPageUsingPost(
  body: API.SpaceQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPageSpaceVO_>('/api/space/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateSpace POST /api/space/update */
export async function updateSpaceUsingPost(
  body: API.SpaceUpdateDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
