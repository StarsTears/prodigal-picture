// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** addUser POST /api/user/add */
export async function addUserUsingPost(body: API.UserAddDTO, options?: { [key: string]: any }) {
  return request<API.BaseResultLong_>('/api/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteUser DELETE /api/user/delete */
export async function deleteUserUsingDelete(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/user/delete', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editUser POST /api/user/edit */
export async function editUserUsingPost(body: API.UserUpdateDTO, options?: { [key: string]: any }) {
  return request<API.BaseResultBoolean_>('/api/user/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getUserByID GET /api/user/get */
export async function getUserByIdUsingGet(
  params: API.getUserByIDUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultUser_>('/api/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getUserVOByID GET /api/user/get/vo */
export async function getUserVoByIdUsingGet(
  params: API.getUserVOByIDUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultUserVO_>('/api/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getLoginUser GET /api/user/getLoginUser */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultUserVO_>('/api/user/getLoginUser', {
    method: 'GET',
    ...(options || {}),
  })
}

/** listUserVOByPage POST /api/user/list/page/vo */
export async function listUserVoByPageUsingPost(
  body: API.UserQueryDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPageUserVO_>('/api/user/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateUser POST /api/user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
