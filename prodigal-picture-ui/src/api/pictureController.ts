// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** deletePicture POST /api/picture/delete */
export async function deletePictureUsingPost(
  body: API.PictureDeleteDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/picture/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editPicture POST /api/picture/edit */
export async function editPictureUsingPost(
  body: API.PictureEditDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/picture/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editPictureByBatch POST /api/picture/edit/batch */
export async function editPictureByBatchUsingPost(
  body: API.PictureEditByBatchDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/picture/edit/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getPictureByID POST /api/picture/get */
export async function getPictureByIdUsingPost(
  body: API.PictureGetDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPicture_>('/api/picture/get', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getPictureVOByID POST /api/picture/get/vo */
export async function getPictureVoByIdUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVOByIDUsingPOSTParams,
  body: API.PictureGetDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPictureVO_>('/api/picture/get/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureByPage POST /api/picture/list/page */
export async function listPictureByPageUsingPost(
  body: API.PictureQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPagePicture_>('/api/picture/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureVOByPage POST /api/picture/list/page/vo */
export async function listPictureVoByPageUsingPost(
  body: API.PictureQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPagePictureVO_>('/api/picture/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureVOByPageCache POST /api/picture/list/page/vo/cache */
export async function listPictureVoByPageCacheUsingPost(
  body: API.PictureQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPagePictureVO_>('/api/picture/list/page/vo/cache', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** createPictureOutPaintingTask POST /api/picture/out_painting/create_task */
export async function createPictureOutPaintingTaskUsingPost(
  body: API.CreatePictureOutPaintingTaskDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultCreateOutPaintingTaskVO_>('/api/picture/out_painting/create_task', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getPictureOutPaintingTask GET /api/picture/out_painting/get_task */
export async function getPictureOutPaintingTaskUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureOutPaintingTaskUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultGetOutPaintingTaskVO_>('/api/picture/out_painting/get_task', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** doPictureReview POST /api/picture/review */
export async function doPictureReviewUsingPost(
  body: API.PictureReviewDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/picture/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** searchImageByBaidu POST /api/picture/search/picture */
export async function searchImageByBaiduUsingPost(
  body: API.ImageSearchDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultListImageSearchResult_>('/api/picture/search/picture', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureTagCategory GET /api/picture/tag_category */
export async function listPictureTagCategoryUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultPictureTagCategory_>('/api/picture/tag_category', {
    method: 'GET',
    ...(options || {}),
  })
}

/** testSharding POST /api/picture/test/sharding */
export async function testShardingUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResultListPicture_>('/api/picture/test/sharding', {
    method: 'POST',
    ...(options || {}),
  })
}

/** updatePicture POST /api/picture/update */
export async function updatePictureUsingPost(
  body: API.PictureUpdateDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultBoolean_>('/api/picture/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** uploadPicture POST /api/picture/upload */
export async function uploadPictureUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureUsingPOSTParams,
  body: {},
  multipartFile?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (multipartFile) {
    formData.append('multipartFile', multipartFile)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, JSON.stringify(item))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResultPictureVO_>('/api/picture/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** uploadPictureByBatch POST /api/picture/upload/batch */
export async function uploadPictureByBatchUsingPost(
  body: API.PictureUploadByBatchDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultInt_>('/api/picture/upload/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** uploadPictureByUrl POST /api/picture/upload/url */
export async function uploadPictureByUrlUsingPost(
  body: API.PictureUploadDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResultPictureVO_>('/api/picture/upload/url', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
