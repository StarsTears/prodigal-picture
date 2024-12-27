declare namespace API {
  type BaseResultBoolean_ = {
    code?: number
    data?: boolean
    msg?: string
    status?: boolean
  }

  type BaseResultCreateOutPaintingTaskVO_ = {
    code?: number
    data?: CreateOutPaintingTaskVO
    msg?: string
    status?: boolean
  }

  type BaseResultGetOutPaintingTaskVO_ = {
    code?: number
    data?: GetOutPaintingTaskVO
    msg?: string
    status?: boolean
  }

  type BaseResultInt_ = {
    code?: number
    data?: number
    msg?: string
    status?: boolean
  }

  type BaseResultListImageSearchResult_ = {
    code?: number
    data?: ImageSearchResult[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceLevel_ = {
    code?: number
    data?: SpaceLevel[]
    msg?: string
    status?: boolean
  }

  type BaseResultLong_ = {
    code?: number
    data?: number
    msg?: string
    status?: boolean
  }

  type BaseResultPagePicture_ = {
    code?: number
    data?: PagePicture_
    msg?: string
    status?: boolean
  }

  type BaseResultPagePictureVO_ = {
    code?: number
    data?: PagePictureVO_
    msg?: string
    status?: boolean
  }

  type BaseResultPageSpace_ = {
    code?: number
    data?: PageSpace_
    msg?: string
    status?: boolean
  }

  type BaseResultPageSpaceVO_ = {
    code?: number
    data?: PageSpaceVO_
    msg?: string
    status?: boolean
  }

  type BaseResultPageUserVO_ = {
    code?: number
    data?: PageUserVO_
    msg?: string
    status?: boolean
  }

  type BaseResultPicture_ = {
    code?: number
    data?: Picture
    msg?: string
    status?: boolean
  }

  type BaseResultPictureTagCategory_ = {
    code?: number
    data?: PictureTagCategory
    msg?: string
    status?: boolean
  }

  type BaseResultPictureVO_ = {
    code?: number
    data?: PictureVO
    msg?: string
    status?: boolean
  }

  type BaseResultSpace_ = {
    code?: number
    data?: Space
    msg?: string
    status?: boolean
  }

  type BaseResultSpaceVO_ = {
    code?: number
    data?: SpaceVO
    msg?: string
    status?: boolean
  }

  type BaseResultString_ = {
    code?: number
    data?: string
    msg?: string
    status?: boolean
  }

  type BaseResultUser_ = {
    code?: number
    data?: User
    msg?: string
    status?: boolean
  }

  type BaseResultUserVO_ = {
    code?: number
    data?: UserVO
    msg?: string
    status?: boolean
  }

  type CreateOutPaintingTaskVO = {
    code?: string
    message?: string
    output?: Output
    requestId?: string
  }

  type CreatePictureOutPaintingTaskDto = {
    parameters?: Parameters
    pictureId?: number
  }

  type deleteCacheUsingPOSTParams = {
    /** key */
    key?: string
    /** type */
    type?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type GetOutPaintingTaskVO = {
    output?: Output1
    requestId?: string
  }

  type getPictureByIDUsingGETParams = {
    /** id */
    id?: number
  }

  type getPictureOutPaintingTaskUsingGETParams = {
    /** taskId */
    taskId?: string
  }

  type getPictureVOUsingGETParams = {
    /** id */
    id?: number
  }

  type getSpaceByIDUsingGETParams = {
    /** id */
    id?: number
  }

  type getSpaceVOByIDUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserByIDUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserVOByIDUsingGETParams = {
    /** id */
    id?: number
  }

  type ImageSearchDto = {
    pictureId?: number
  }

  type ImageSearchResult = {
    fromUrl?: string
    objUrl?: string
    thumbUrl?: string
  }

  type LoginDto = {
    userAccount?: string
    userPassword?: string
  }

  type Output = {
    taskId?: string
    taskStatus?: string
  }

  type Output1 = {
    code?: string
    endTime?: string
    message?: string
    outputImageUrl?: string
    scheduledTime?: string
    submitTime?: string
    taskId?: string
    taskMetrics?: TaskMetrics
    taskStatus?: string
  }

  type PagePicture_ = {
    current?: number
    pages?: number
    records?: Picture[]
    size?: number
    total?: number
  }

  type PagePictureVO_ = {
    current?: number
    pages?: number
    records?: PictureVO[]
    size?: number
    total?: number
  }

  type PageSpace_ = {
    current?: number
    pages?: number
    records?: Space[]
    size?: number
    total?: number
  }

  type PageSpaceVO_ = {
    current?: number
    pages?: number
    records?: SpaceVO[]
    size?: number
    total?: number
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type Parameters = {
    addWatermark?: boolean
    angle?: number
    bestQuality?: boolean
    bottomOffset?: number
    leftOffset?: number
    limitImageSize?: boolean
    outputRatio?: string
    rightOffset?: number
    topOffset?: number
    xScale?: number
    yScale?: number
  }

  type Picture = {
    category?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    isDelete?: number
    name?: string
    originUrl?: string
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: number
    sourceUrl?: string
    spaceId?: number
    tags?: string
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    userId?: number
  }

  type PictureEditByBatchDto = {
    category?: string
    nameRule?: string
    pictureIdList?: number[]
    spaceId?: number
    tags?: string[]
  }

  type PictureEditDto = {
    category?: string
    id?: number
    introduction?: string
    name?: string
    spaceId?: number
    tags?: string[]
    userId?: number
  }

  type PictureQueryDto = {
    category?: string
    current?: number
    endEditTime?: string
    id?: number
    introduction?: string
    name?: string
    nullSpaceId?: boolean
    pageSize?: number
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewerId?: number
    searchText?: string
    sortField?: string
    sortOrder?: string
    spaceId?: number
    startEditTime?: string
    tags?: string[]
    userId?: number
  }

  type PictureReviewDto = {
    id?: number
    reviewMessage?: string
    reviewStatus?: number
  }

  type PictureTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type PictureUpdateDto = {
    category?: string
    id?: number
    introduction?: string
    name?: string
    tags?: string[]
  }

  type PictureUploadByBatchDto = {
    category?: string
    count?: number
    namePrefix?: string
    offset?: number
    searchText?: string
    tags?: string[]
    url?: string
  }

  type PictureUploadDto = {
    fileUrl?: string
    id?: number
    picName?: string
    spaceId?: number
  }

  type PictureVO = {
    category?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    name?: string
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    spaceId?: number
    tags?: string[]
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    user?: UserVO
    userId?: number
  }

  type RegisterDto = {
    checkPassword?: string
    userAccount?: string
    userPassword?: string
  }

  type Space = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    totalCount?: number
    totalSize?: number
    updateTime?: string
    userId?: number
  }

  type SpaceAddDto = {
    spaceLevel?: number
    spaceName?: string
    userId?: number
  }

  type SpaceEditDto = {
    id?: number
    spaceName?: string
  }

  type SpaceLevel = {
    maxCount?: number
    maxSize?: number
    text?: string
    value?: number
  }

  type SpaceQueryDto = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    spaceLevel?: number
    spaceName?: string
    userId?: number
  }

  type SpaceUpdateDto = {
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceVO = {
    createTime?: string
    editTime?: string
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    totalCount?: number
    totalSize?: number
    updateTime?: string
    user?: UserVO
    userId?: number
  }

  type TaskMetrics = {
    failed?: number
    succeeded?: number
    total?: number
  }

  type testDownloadFileUsingGETParams = {
    /** filepath */
    filepath?: string
  }

  type uploadPictureUsingPOSTParams = {
    fileUrl?: string
    id?: number
    picName?: string
    spaceId?: number
  }

  type User = {
    createTime?: string
    editTime?: string
    id?: number
    inviteUser?: number
    isDelete?: number
    shareCode?: string
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
    vipNumber?: number
  }

  type UserAddDto = {
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserQueryDto = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserUpdateDto = {
    id?: number
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    editTime?: string
    id?: number
    inviteUser?: number
    shareCode?: string
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
    vipNumber?: number
  }
}
