// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** deleteCache POST /api/cache/delete */
export async function deleteCacheUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteCacheUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/cache/delete', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
