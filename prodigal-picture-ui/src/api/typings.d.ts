declare namespace API {
  type BaseResultBoolean_ = {
    code?: number
    data?: boolean
    msg?: string
    status?: boolean
  }

  type BaseResultLong_ = {
    code?: number
    data?: number
    msg?: string
    status?: boolean
  }

  type BaseResultPageUserVO_ = {
    code?: number
    data?: PageUserVO_
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

  type DeleteRequest = {
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

  type LoginDto = {
    userAccount?: string
    userPassword?: string
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type RegisterDto = {
    checkPassword?: string
    userAccount?: string
    userPassword?: string
  }

  type User = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
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

  type UserVO = {
    createTime?: string
    editTime?: string
    id?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
