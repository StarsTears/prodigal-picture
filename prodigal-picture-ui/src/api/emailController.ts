// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** addEmail POST /api/email/add */
export async function addEmailUsingPost(body: API.EmailDto, options?: { [key: string]: any }) {
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

/** getEmailById GET /api/email/get/${param0} */
export async function getEmailByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getEmailByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  const { emailId: param0, ...queryParams } = params
  return request<API.BaseResultEmailVO_>(`/api/email/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** listEmailByPage POST /api/email/list */
export async function listEmailByPageUsingPost(
  body: API.EmailQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPageEmailVO_>('/api/email/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** sendEmail POST /api/email/send */
export async function sendEmailUsingPost(body: API.EmailDto, options?: { [key: string]: any }) {
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

/** updateEmail POST /api/email/update */
export async function updateEmailUsingPost(body: API.EmailDto, options?: { [key: string]: any }) {
  return request<API.BaseResultBoolean_>('/api/email/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
