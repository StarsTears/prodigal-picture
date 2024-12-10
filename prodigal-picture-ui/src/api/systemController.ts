// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** addUser POST /api/sys/addUser */
export async function addUserUsingPost(body: API.UserAddDto, options?: { [key: string]: any }) {
  return request<API.BaseResultLong_>('/api/sys/addUser', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteUser DELETE /api/sys/delete */
export async function deleteUserUsingDelete(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/sys/delete', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getUserByID GET /api/sys/get */
export async function getUserByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIDUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultUser_>('/api/sys/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getUserVOByID GET /api/sys/get/vo */
export async function getUserVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVOByIDUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultUserVO_>('/api/sys/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getLoginUser GET /api/sys/getLoginUser */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultUserVO_>('/api/sys/getLoginUser', {
    method: 'GET',
    ...(options || {}),
  })
}

/** hello GET /api/sys/hello */
export async function helloUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultString_>('/api/sys/hello', {
    method: 'GET',
    ...(options || {}),
  })
}

/** listUserVOByPage POST /api/sys/list/page/vo */
export async function listUserVoByPageUsingPost(
  body: API.UserQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPageUserVO_>('/api/sys/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** login POST /api/sys/login */
export async function loginUsingPost(body: API.LoginDto, options?: { [key: string]: any }) {
  return request<API.BaseResultUserVO_>('/api/sys/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** logout POST /api/sys/logout */
export async function logoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResultBoolean_>('/api/sys/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** register POST /api/sys/register */
export async function registerUsingPost(body: API.RegisterDto, options?: { [key: string]: any }) {
  return request<API.BaseResultString_>('/api/sys/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateUser POST /api/sys/update */
export async function updateUserUsingPost(
  body: API.UserQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/sys/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
