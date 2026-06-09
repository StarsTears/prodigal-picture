declare namespace API {
  type PageRequest = {
    current?: number
    pageSize?: number
  }

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

  type BaseResultEmailVO_ = {
    code?: number
    data?: EmailVO
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

  type BaseResultListPicture_ = {
    code?: number
    data?: Picture[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpace_ = {
    code?: number
    data?: Space[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceCategoryAnalyzeVO_ = {
    code?: number
    data?: SpaceCategoryAnalyzeVO[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceLevel_ = {
    code?: number
    data?: SpaceLevel[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceSizeAnalyzeVO_ = {
    code?: number
    data?: SpaceSizeAnalyzeVO[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceTagAnalyzeVO_ = {
    code?: number
    data?: SpaceTagAnalyzeVO[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceUserAnalyzeVO_ = {
    code?: number
    data?: SpaceUserAnalyzeVO[]
    msg?: string
    status?: boolean
  }

  type BaseResultListSpaceUserVO_ = {
    code?: number
    data?: SpaceUserVO[]
    msg?: string
    status?: boolean
  }

  type BaseResultLong_ = {
    code?: number
    data?: number
    msg?: string
    status?: boolean
  }

  type BaseResultPageEmailVO_ = {
    code?: number
    data?: PageEmailVO_
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

  type BaseResultPageSpaceUserVO_ = {
    code?: number
    data?: PageSpaceUserVO_
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

  type BaseResultSpaceUsageAnalyzeVO_ = {
    code?: number
    data?: SpaceUsageAnalyzeVO
    msg?: string
    status?: boolean
  }

  type BaseResultSpaceUser_ = {
    code?: number
    data?: SpaceUser
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

  type CreatePictureOutPaintingTaskDTO = {
    parameters?: Parameters
    pictureId?: number
  }

  type deleteCacheUsingPOSTParams = {
    /** key */
    key?: string
    /** type */
    type?: string
  }

  type deleteEmailUsingPOSTParams = {
    /** emailId */
    emailId: string
  }

  type DeleteRequest = {
    id?: number
  }

  type EmailAddDTO = {
    attachments?: string[]
    html?: boolean
    subject?: string
    to?: string
    txt?: string
    type?: number
  }

  type EmailUpdateDTO = {
    attachments?: string[]
    html?: boolean
    id?: string
    status?: number
    subject?: string
    to?: string
    txt?: string
    type?: number
  }

  type EmailSendDTO = {
    attachments?: string[]
    html?: boolean
    subject?: string
    to?: string
    txt?: string
    type?: number
  }

  type EmailDTO = {
    attachments?: string[]
    html?: boolean
    id?: string
    receiveUserId?: number
    status?: number
    subject?: string
    to?: string
    txt?: string
    type?: number
  }

  type EmailQueryDTO = {
    current?: number
    id?: string
    pageSize?: number
    sortField?: string
    sortOrder?: string
    status?: number
    subject?: string
    to?: string
    txt?: string
    type?: number
  }

  type EmailRequest = {
    email?: string
  }

  type EmailVO = {
    attachments?: string
    createTime?: string
    createUserId?: number
    html?: boolean
    id?: string
    receiveUserId?: number
    receiveUserVO?: UserVO
    sendTime?: string
    sendUserId?: number
    status?: number
    subject?: string
    to?: string
    txt?: string
    type?: number
    updateTime?: string
    updateUserId?: number
  }

  type getEmailByIdUsingGETParams = {
    /** emailId */
    emailId: string
  }

  type GetOutPaintingTaskVO = {
    output?: Output1
    requestId?: string
  }

  type getPictureOutPaintingTaskUsingGETParams = {
    /** taskId */
    taskId?: string
  }

  type getPictureVOByIDUsingPOSTParams = {
    /** isView */
    isView?: boolean
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

  type ImageSearchDTO = {
    pictureId?: number
    spaceId?: number
  }

  type ImageSearchResult = {
    fromUrl?: string
    objUrl?: string
    thumbUrl?: string
  }

  type LoginDTO = {
    captcha?: string
    email?: string
    loginType?: string
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

  type PageEmailVO_ = {
    current?: number
    pages?: number
    records?: EmailVO[]
    size?: number
    total?: number
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

  type PageSpaceUserVO_ = {
    current?: number
    pages?: number
    records?: SpaceUserVO[]
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
    shareQuantity?: number
    sourceUrl?: string
    spaceId?: number
    tags?: string
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    userId?: number
    viewQuantity?: number
  }

  type PictureDeleteDTO = {
    id?: number
    spaceId?: number
  }

  type PictureEditByBatchDTO = {
    category?: string
    nameRule?: string
    pictureIdList?: number[]
    spaceId?: number
    tags?: string[]
  }

  type PictureEditDTO = {
    category?: string
    id?: number
    introduction?: string
    name?: string
    spaceId?: number
    tags?: string[]
    userId?: number
  }

  type PictureGetDTO = {
    id?: number
    spaceId?: number
  }

  type PictureQueryDTO = {
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

  type PictureReviewDTO = {
    id?: number
    reviewMessage?: string
    reviewStatus?: number
    spaceId?: number
  }

  type PictureTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type PictureUpdateDTO = {
    category?: string
    id?: number
    introduction?: string
    name?: string
    spaceId?: number
    tags?: string[]
  }

  type PictureUploadByBatchDTO = {
    category?: string
    count?: number
    namePrefix?: string
    offset?: number
    searchText?: string
    tags?: string[]
    url?: string
  }

  type PictureUploadDTO = {
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
    permissionList?: string[]
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    shareQuantity?: number
    spaceId?: number
    tags?: string[]
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    user?: UserVO
    userId?: number
    viewQuantity?: number
  }

  type RegisterDTO = {
    checkPassword?: string
    userAccount?: string
    userEmail?: string
    userName?: string
    userPassword?: string
  }

  type sendEmailByIdUsingPOSTParams = {
    /** emailId */
    emailId: string
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
    spaceType?: number
    totalCount?: number
    totalSize?: number
    updateTime?: string
    userId?: number
  }

  type SpaceAddDTO = {
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    userId?: number
  }

  type SpaceCategoryAnalyzeDTO = {
    queryAll?: boolean
    queryPublic?: boolean
    spaceId?: number
  }

  type SpaceCategoryAnalyzeVO = {
    category?: string
    count?: number
    totalSize?: number
  }

  type SpaceEditDTO = {
    id?: number
    spaceName?: string
  }

  type SpaceLevel = {
    maxCount?: number
    maxSize?: number
    text?: string
    value?: number
  }

  type SpaceQueryDTO = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    userId?: number
  }

  type SpaceRankAnalyzeDTO = {
    topN?: number
  }

  type SpaceSizeAnalyzeDTO = {
    queryAll?: boolean
    queryPublic?: boolean
    spaceId?: number
  }

  type SpaceSizeAnalyzeVO = {
    count?: number
    sizeRange?: string
  }

  type SpaceTagAnalyzeDTO = {
    queryAll?: boolean
    queryPublic?: boolean
    spaceId?: number
  }

  type SpaceTagAnalyzeVO = {
    count?: number
    tag?: string
  }

  type SpaceUpdateDTO = {
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceUsageAnalyzeDTO = {
    queryAll?: boolean
    queryPublic?: boolean
    spaceId?: number
  }

  type SpaceUsageAnalyzeVO = {
    countUsageRatio?: number
    maxCount?: number
    maxSize?: number
    sizeUsageRatio?: number
    usedCount?: number
    usedSize?: number
  }

  type SpaceUser = {
    createTime?: string
    id?: number
    spaceId?: number
    spaceRole?: string
    updateTime?: string
    userId?: number
  }

  type SpaceUserAddDTO = {
    id?: number
    spaceId?: number
    spaceRole?: string
    userId?: number
  }

  type SpaceUserAnalyzeDTO = {
    queryAll?: boolean
    queryPublic?: boolean
    spaceId?: number
    timeDimension?: string
    userId?: number
  }

  type SpaceUserAnalyzeVO = {
    count?: number
    timeRange?: string
  }

  type SpaceUserEditDTO = {
    id?: number
    spaceRole?: string
  }

  type SpaceUserQueryDTO = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    spaceId?: number
    spaceRole?: string
    userId?: number
  }

  type SpaceUserVO = {
    createTime?: string
    id?: number
    space?: SpaceVO
    spaceId?: number
    spaceRole?: string
    updateTime?: string
    user?: UserVO
    userId?: number
  }

  type SpaceVO = {
    createTime?: string
    editTime?: string
    id?: number
    maxCount?: number
    maxSize?: number
    permissionList?: string[]
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
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
    userEmail?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
    vipNumber?: number
  }

  type UserAddDTO = {
    userAccount?: string
    userAvatar?: string
    userEmail?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserQueryDTO = {
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

  type UserUpdateDTO = {
    id?: number
    userAccount?: string
    userAvatar?: string
    userEmail?: string
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
    userEmail?: string
    userName?: string
    userProfile?: string
    userRole?: string
    vipNumber?: number
  }

  type ResetPasswordDTO = {
    userAccount?: string
    userEmail?: string
    captcha?: string
    newPassword?: string
    checkPassword?: string
  }
}
