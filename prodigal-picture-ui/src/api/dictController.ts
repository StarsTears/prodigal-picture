// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** listDictTypes GET /api/dict/types */
export async function listDictTypesUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultListString_>('/api/dict/types', {
    method: 'GET',
    ...(options || {}),
  })
}

/** listDictByType GET /api/dict/list/{dictType} */
export async function listDictByTypeUsingGet(
  dictType: string,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListDict_>(`/api/dict/list/${dictType}`, {
    method: 'GET',
    ...(options || {}),
  })
}

/** addDict POST /api/dict/add */
export async function addDictUsingPost(
  body: API.DictDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/dict/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateDict POST /api/dict/update */
export async function updateDictUsingPost(
  body: API.DictDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/dict/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteDict POST /api/dict/delete */
export async function deleteDictUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/dict/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
