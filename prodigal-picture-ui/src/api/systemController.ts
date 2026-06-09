// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** hello GET /api/sys/hello */
export async function helloUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultString_>('/api/sys/hello', {
    method: 'GET',
    ...(options || {}),
  })
}

/** login POST /api/sys/login */
export async function loginUsingPost(body: API.LoginDTO, options?: { [key: string]: any }) {
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
export async function registerUsingPost(body: API.RegisterDTO, options?: { [key: string]: any }) {
  return request<API.BaseResultString_>('/api/sys/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** resetPassword POST /api/sys/reset-password */
export async function resetPasswordUsingPost(body: API.ResetPasswordDTO, options?: { [key: string]: any }) {
  return request<API.BaseResultBoolean_>('/api/sys/reset-password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
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
