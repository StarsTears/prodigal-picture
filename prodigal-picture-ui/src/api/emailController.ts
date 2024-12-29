// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** addEmail POST /api/email/add */
export async function addEmailUsingPost(body: API.SendEmailDto, options?: { [key: string]: any }) {
  return request<API.BaseResultString_>('/api/email/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteEmail POST /api/email/delete/${param0} */
export async function deleteEmailUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteEmailUsingPOSTParams,
  options?: { [key: string]: any }
) {
  const { emailId: param0, ...queryParams } = params
  return request<API.BaseResultBoolean_>(`/api/email/delete/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** editEmail POST /api/email/edit */
export async function editEmailUsingPost(body: API.SendEmailDto, options?: { [key: string]: any }) {
  return request<API.BaseResultBoolean_>('/api/email/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getEmailById GET /api/email/get/${param0} */
export async function getEmailByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getEmailByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  const { emailId: param0, ...queryParams } = params
  return request<API.BaseResultEmail_>(`/api/email/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** listEmail POST /api/email/list */
export async function listEmailUsingPost(
  body: API.QueryEmailDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListEmail_>('/api/email/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** sendEmail POST /api/email/send */
export async function sendEmailUsingPost(body: API.SendEmailDto, options?: { [key: string]: any }) {
  return request<API.BaseResultBoolean_>('/api/email/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** sendEmailById POST /api/email/send/${param0} */
export async function sendEmailByIdUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendEmailByIdUsingPOSTParams,
  options?: { [key: string]: any }
) {
  const { emailId: param0, ...queryParams } = params
  return request<API.BaseResultBoolean_>(`/api/email/send/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}
